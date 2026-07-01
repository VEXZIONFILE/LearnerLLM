from datetime import date

from fastapi import HTTPException, status

from learner_api.config import Settings
from learner_api.database import D1Database
from learner_api.models import User
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

    async def get_status(self, db: D1Database, user: User) -> ScanQuotaResponse:
        used = await self._read_today_count(db, user.uid)
        return self.build_status(used, user.subscription_tier)

    async def record_scan(self, db: D1Database, user: User) -> ScanQuotaResponse:
        if is_premium_tier(user.subscription_tier):
            return self.build_status(0, user.subscription_tier)

        today = date.today().isoformat()
        row = await db.fetchone(
            "SELECT * FROM scan_usage WHERE user_uid = ? AND usage_date = ?",
            user.uid,
            today,
        )
        count = int(row["count"]) if row else 0
        if count >= self.settings.free_daily_scan_limit:
            raise HTTPException(
                status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                detail="Daily scan limit reached. Upgrade to Premium for unlimited homework scans.",
            )

        if row is None:
            await db.execute(
                "INSERT INTO scan_usage (user_uid, usage_date, count) VALUES (?, ?, 1)",
                user.uid,
                today,
            )
            count = 1
        else:
            count += 1
            await db.execute(
                "UPDATE scan_usage SET count = ?, updated_at = datetime('now') WHERE id = ?",
                count,
                row["id"],
            )

        return self.build_status(count, user.subscription_tier)

    async def _read_today_count(self, db: D1Database, uid: str) -> int:
        today = date.today().isoformat()
        row = await db.fetchone(
            "SELECT count FROM scan_usage WHERE user_uid = ? AND usage_date = ?",
            uid,
            today,
        )
        return int(row["count"]) if row else 0
