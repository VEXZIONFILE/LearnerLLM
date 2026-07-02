from datetime import date

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.config import Settings
from learner_api.models import MessageUsage, User
from learner_api.schemas import MessageQuotaResponse
from learner_api.services.model_registry import is_premium_tier


class MessageQuotaService:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings

    def build_status(self, used_today: int, tier: str) -> MessageQuotaResponse:
        premium = is_premium_tier(tier)
        if premium:
            return MessageQuotaResponse(
                used_today=used_today,
                daily_limit=None,
                remaining=None,
                is_premium=True,
                can_send=True,
                quota_label="Unlimited messages",
                max_message_length=8000,
            )
        limit = self.settings.free_daily_message_limit
        remaining = max(0, limit - used_today)
        return MessageQuotaResponse(
            used_today=used_today,
            daily_limit=limit,
            remaining=remaining,
            is_premium=False,
            can_send=remaining > 0,
            quota_label=f"{remaining} of {limit} messages left today",
            max_message_length=self.settings.free_max_message_length,
        )

    async def get_status(self, db: AsyncSession, user: User) -> MessageQuotaResponse:
        used = await self._read_today_count(db, user.uid)
        return self.build_status(used, user.subscription_tier)

    async def ensure_can_send(self, db: AsyncSession, user: User, tier: str, message: str) -> None:
        quota = self.build_status(await self._read_today_count(db, user.uid), tier)
        if len(message) > quota.max_message_length:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=(
                    f"Message is too long for the Standard plan "
                    f"({quota.max_message_length} characters max). "
                    "Upgrade for longer messages."
                ),
            )
        if not quota.can_send:
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=(
                    "Daily message limit reached. Upgrade to Pro for unlimited chat messages."
                ),
            )

    async def record_message(self, db: AsyncSession, user: User) -> MessageQuotaResponse:
        if is_premium_tier(user.subscription_tier):
            return self.build_status(0, user.subscription_tier)

        today = date.today().isoformat()
        result = await db.execute(
            select(MessageUsage).where(
                MessageUsage.user_uid == user.uid,
                MessageUsage.usage_date == today,
            )
        )
        usage = result.scalar_one_or_none()
        if usage is None:
            usage = MessageUsage(user_uid=user.uid, usage_date=today, count=0)
            db.add(usage)

        if usage.count >= self.settings.free_daily_message_limit:
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail=(
                    "Daily message limit reached. Upgrade to Pro for unlimited chat messages."
                ),
            )

        usage.count += 1
        await db.commit()
        await db.refresh(usage)
        return self.build_status(usage.count, user.subscription_tier)

    async def _read_today_count(self, db: AsyncSession, uid: str) -> int:
        today = date.today().isoformat()
        result = await db.execute(
            select(MessageUsage).where(
                MessageUsage.user_uid == uid,
                MessageUsage.usage_date == today,
            )
        )
        usage = result.scalar_one_or_none()
        return usage.count if usage else 0
