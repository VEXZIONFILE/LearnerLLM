from datetime import date

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.config import Settings
from learner_api.models import ScanUsage, User
from learner_api.schemas import ScanQuotaResponse
from learner_api.services.model_registry import is_premium_tier


class ScanQuotaService:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings

    def build_status(self, used_today: int, tier: str) -> ScanQuotaResponse:
        premium = is_premium_tier(tier)
        if premium:
            return ScanQuotaResponse(
                used_today=used_today,
                daily_limit=None,
                remaining=None,
                is_premium=True,
                can_scan=True,
                quota_label="Unlimited scans",
            )
        limit = self.settings.free_daily_scan_limit
        remaining = max(0, limit - used_today)
        return ScanQuotaResponse(
            used_today=used_today,
            daily_limit=limit,
            remaining=remaining,
            is_premium=False,
            can_scan=remaining > 0,
            quota_label=f"{remaining} of {limit} scans left today",
        )

    async def get_status(self, db: AsyncSession, user: User) -> ScanQuotaResponse:
        used = await self._read_today_count(db, user.uid)
        return self.build_status(used, user.subscription_tier)

    async def record_scan(self, db: AsyncSession, user: User) -> ScanQuotaResponse:
        if is_premium_tier(user.subscription_tier):
            return self.build_status(0, user.subscription_tier)

        today = date.today().isoformat()
        result = await db.execute(
            select(ScanUsage).where(
                ScanUsage.user_uid == user.uid,
                ScanUsage.usage_date == today,
            )
        )
        usage = result.scalar_one_or_none()
        if usage is None:
            usage = ScanUsage(user_uid=user.uid, usage_date=today, count=0)
            db.add(usage)

        if usage.count >= self.settings.free_daily_scan_limit:
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Daily scan limit reached. Upgrade to Premium for unlimited homework scans.",
            )

        usage.count += 1
        await db.commit()
        await db.refresh(usage)
        return self.build_status(usage.count, user.subscription_tier)

    async def _read_today_count(self, db: AsyncSession, uid: str) -> int:
        today = date.today().isoformat()
        result = await db.execute(
            select(ScanUsage).where(ScanUsage.user_uid == uid, ScanUsage.usage_date == today)
        )
        usage = result.scalar_one_or_none()
        return usage.count if usage else 0
