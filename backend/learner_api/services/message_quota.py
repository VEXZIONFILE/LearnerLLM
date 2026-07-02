from datetime import date

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.config import Settings
from learner_api.models import MessageUsage, User
from learner_api.schemas import AppMode, MessageQuotaResponse
from learner_api.services.billing import MEGA_PRODUCT_ID


MODE_LABELS = {
    AppMode.TUTOR: "Tutor",
    AppMode.STUDY: "Study",
    AppMode.CODE: "Code",
    AppMode.FREE: "Free",
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

    def build_status(
        self,
        used_today: int,
        tier: str,
        app_mode: AppMode,
        active_product_id: str | None,
    ) -> MessageQuotaResponse:
        limit = self.daily_limit_for(tier, active_product_id)
        mode_label = MODE_LABELS.get(app_mode, app_mode.value.title())
        if limit is None:
            return MessageQuotaResponse(
                used_today=used_today,
                daily_limit=None,
                remaining=None,
                is_premium=True,
                can_send=True,
                quota_label=f"Unlimited messages in {mode_label}",
                app_mode=app_mode,
            )
        remaining = max(0, limit - used_today)
        return MessageQuotaResponse(
            used_today=used_today,
            daily_limit=limit,
            remaining=remaining,
            is_premium=False,
            can_send=remaining > 0,
            quota_label=f"{remaining} of {limit} {mode_label} messages left today",
            app_mode=app_mode,
        )

    async def get_status(
        self,
        db: AsyncSession,
        user: User,
        app_mode: AppMode,
        active_product_id: str | None,
    ) -> MessageQuotaResponse:
        used = await self._read_today_count(db, user.uid, app_mode)
        return self.build_status(used, user.subscription_tier, app_mode, active_product_id)

    async def ensure_can_send(
        self,
        db: AsyncSession,
        user: User,
        tier: str,
        app_mode: AppMode,
        active_product_id: str | None,
    ) -> None:
        quota = self.build_status(
            await self._read_today_count(db, user.uid, app_mode),
            tier,
            app_mode,
            active_product_id,
        )
        if not quota.can_send:
            limit = quota.daily_limit or 0
            mode_label = MODE_LABELS.get(app_mode, app_mode.value.title())
            if tier == "FREE":
                detail = (
                    f"Daily {mode_label} message limit reached ({limit}/day). "
                    "Upgrade to Pro for more messages."
                )
            else:
                detail = (
                    f"Daily {mode_label} message limit reached ({limit}/day). "
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
        active_product_id: str | None,
    ) -> MessageQuotaResponse:
        limit = self.daily_limit_for(user.subscription_tier, active_product_id)
        if limit is None:
            return self.build_status(0, user.subscription_tier, app_mode, active_product_id)

        today = date.today().isoformat()
        mode_key = app_mode.value
        result = await db.execute(
            select(MessageUsage).where(
                MessageUsage.user_uid == user.uid,
                MessageUsage.usage_date == today,
                MessageUsage.app_mode == mode_key,
            )
        )
        usage = result.scalar_one_or_none()
        if usage is None:
            usage = MessageUsage(user_uid=user.uid, usage_date=today, app_mode=mode_key, count=0)
            db.add(usage)

        if usage.count >= limit:
            mode_label = MODE_LABELS.get(app_mode, mode_key.title())
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=f"Daily {mode_label} message limit reached. Upgrade for more messages.",
            )

        usage.count += 1
        await db.commit()
        await db.refresh(usage)
        return self.build_status(usage.count, user.subscription_tier, app_mode, active_product_id)

    async def _read_today_count(self, db: AsyncSession, uid: str, app_mode: AppMode) -> int:
        today = date.today().isoformat()
        result = await db.execute(
            select(MessageUsage).where(
                MessageUsage.user_uid == uid,
                MessageUsage.usage_date == today,
                MessageUsage.app_mode == app_mode.value,
            )
        )
        usage = result.scalar_one_or_none()
        return usage.count if usage else 0
