from datetime import date

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.config import Settings
from learner_api.models import MessageUsage, User
from learner_api.schemas import AppMode, FreeModelVariant, MessageQuotaResponse
from learner_api.services.billing import MEGA_PRODUCT_ID
from learner_api.services.model_registry import default_variant_for_mode


MODE_LABELS = {
    AppMode.TUTOR: "Tutor",
    AppMode.STUDY: "Study",
    AppMode.CODE: "Code",
    AppMode.FREE: "Free",
}

VARIANT_LABELS = {
    FreeModelVariant.TUTOR: "GPT-OSS",
    FreeModelVariant.STUDY: "Nemotron",
    FreeModelVariant.CODE: "Laguna",
}


class MessageQuotaService:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings

    def daily_limit_for(self, tier: str, active_product_id: str | None) -> int | None:
        if tier == "PRO" or active_product_id == MEGA_PRODUCT_ID:
            return None
        if tier == "BASIC":
            return self.settings.pro_daily_message_limit
        return self.settings.free_daily_message_limit

    def quota_bucket(
        self,
        tier: str,
        app_mode: AppMode,
        free_model_variant: FreeModelVariant | None,
    ) -> str:
        if tier == "FREE":
            variant = free_model_variant or default_variant_for_mode(app_mode)
            return variant.value
        return app_mode.value

    def bucket_label(
        self,
        tier: str,
        app_mode: AppMode,
        free_model_variant: FreeModelVariant | None,
    ) -> str:
        if tier == "FREE":
            variant = free_model_variant or default_variant_for_mode(app_mode)
            return VARIANT_LABELS.get(variant, variant.value.title())
        mode_label = MODE_LABELS.get(app_mode, app_mode.value.title())
        return f"{mode_label} mode"

    def build_status(
        self,
        used_today: int,
        tier: str,
        app_mode: AppMode,
        free_model_variant: FreeModelVariant | None,
        active_product_id: str | None,
    ) -> MessageQuotaResponse:
        limit = self.daily_limit_for(tier, active_product_id)
        bucket_label = self.bucket_label(tier, app_mode, free_model_variant)
        if limit is None:
            return MessageQuotaResponse(
                used_today=used_today,
                daily_limit=None,
                remaining=None,
                is_premium=True,
                can_send=True,
                quota_label=f"Unlimited messages with {bucket_label}",
                app_mode=app_mode,
                free_model_variant=free_model_variant or default_variant_for_mode(app_mode),
            )
        remaining = max(0, limit - used_today)
        return MessageQuotaResponse(
            used_today=used_today,
            daily_limit=limit,
            remaining=remaining,
            is_premium=False,
            can_send=remaining > 0,
            quota_label=f"{remaining} of {limit} {bucket_label} messages left today",
            app_mode=app_mode,
            free_model_variant=free_model_variant or default_variant_for_mode(app_mode),
        )

    async def get_status(
        self,
        db: AsyncSession,
        user: User,
        app_mode: AppMode,
        free_model_variant: FreeModelVariant | None,
        active_product_id: str | None,
    ) -> MessageQuotaResponse:
        bucket = self.quota_bucket(user.subscription_tier, app_mode, free_model_variant)
        used = await self._read_today_count(db, user.uid, bucket)
        return self.build_status(
            used,
            user.subscription_tier,
            app_mode,
            free_model_variant,
            active_product_id,
        )

    async def ensure_can_send(
        self,
        db: AsyncSession,
        user: User,
        tier: str,
        app_mode: AppMode,
        free_model_variant: FreeModelVariant | None,
        active_product_id: str | None,
    ) -> None:
        bucket = self.quota_bucket(tier, app_mode, free_model_variant)
        quota = self.build_status(
            await self._read_today_count(db, user.uid, bucket),
            tier,
            app_mode,
            free_model_variant,
            active_product_id,
        )
        if not quota.can_send:
            limit = quota.daily_limit or 0
            bucket_label = self.bucket_label(tier, app_mode, free_model_variant)
            if tier == "FREE":
                detail = (
                    f"Daily {bucket_label} message limit reached ({limit}/day across all modes). "
                    "Upgrade to Pro for more messages."
                )
            else:
                detail = (
                    f"Daily {bucket_label} message limit reached ({limit}/day). "
                    "Upgrade to Premium for unlimited messages."
                )
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=detail,
            )

    async def record_message(
        self,
        db: AsyncSession,
        user: User,
        app_mode: AppMode,
        free_model_variant: FreeModelVariant | None,
        active_product_id: str | None,
    ) -> MessageQuotaResponse:
        limit = self.daily_limit_for(user.subscription_tier, active_product_id)
        if limit is None:
            return self.build_status(
                0,
                user.subscription_tier,
                app_mode,
                free_model_variant,
                active_product_id,
            )

        today = date.today().isoformat()
        bucket = self.quota_bucket(user.subscription_tier, app_mode, free_model_variant)
        result = await db.execute(
            select(MessageUsage).where(
                MessageUsage.user_uid == user.uid,
                MessageUsage.usage_date == today,
                MessageUsage.app_mode == bucket,
            )
        )
        usage = result.scalar_one_or_none()
        if usage is None:
            usage = MessageUsage(user_uid=user.uid, usage_date=today, app_mode=bucket, count=0)
            db.add(usage)

        if usage.count >= limit:
            bucket_label = self.bucket_label(
                user.subscription_tier,
                app_mode,
                free_model_variant,
            )
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=f"Daily {bucket_label} message limit reached. Upgrade for more messages.",
            )

        usage.count += 1
        await db.commit()
        await db.refresh(usage)
        return self.build_status(
            usage.count,
            user.subscription_tier,
            app_mode,
            free_model_variant,
            active_product_id,
        )

    async def _read_today_count(self, db: AsyncSession, uid: str, bucket: str) -> int:
        today = date.today().isoformat()
        result = await db.execute(
            select(MessageUsage).where(
                MessageUsage.user_uid == uid,
                MessageUsage.usage_date == today,
                MessageUsage.app_mode == bucket,
            )
        )
        usage = result.scalar_one_or_none()
        return usage.count if usage else 0
