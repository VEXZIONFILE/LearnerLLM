from datetime import datetime, timedelta, timezone

from learner_api.config import Settings
from learner_api.database import D1Database, _parse_dt
from learner_api.models import User
from learner_api.schemas import BillingVerifyResponse


PRODUCT_TIER_MAP = {
    "learnerlm_basic_monthly": "BASIC",
    "learnerlm_pro_yearly": "BASIC",
    "learnerlm_pro_monthly": "PRO",
}


class BillingService:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings

    async def verify_purchase(
        self,
        db: D1Database,
        user: User,
        product_id: str,
        purchase_token: str,
        package_name: str | None,
    ) -> BillingVerifyResponse:
        _ = package_name
        tier = PRODUCT_TIER_MAP.get(product_id)
        if tier is None:
            return BillingVerifyResponse(verified=False, subscription_tier="FREE", product_id=product_id)

        verified = self.settings.billing_verification_disabled or bool(purchase_token.strip())
        expires_at = None
        if tier in {"BASIC", "PRO"}:
            if product_id.endswith("yearly"):
                expires_at = datetime.now(timezone.utc) + timedelta(days=365)
            else:
                expires_at = datetime.now(timezone.utc) + timedelta(days=31)

        if verified:
            await db.execute(
                "UPDATE users SET subscription_tier = ?, updated_at = datetime('now') WHERE uid = ?",
                tier,
                user.uid,
            )
            user.subscription_tier = tier
            await db.execute(
                """
                INSERT INTO subscription_records
                (user_uid, product_id, purchase_token, tier, verified, expires_at)
                VALUES (?, ?, ?, ?, 1, ?)
                """,
                user.uid,
                product_id,
                purchase_token,
                tier,
                expires_at.isoformat() if expires_at else None,
            )

        return BillingVerifyResponse(
            verified=verified,
            subscription_tier=user.subscription_tier,
            product_id=product_id,
        )

    async def refresh_user_tier(self, db: D1Database, user: User) -> str:
        row = await db.fetchone(
            """
            SELECT * FROM subscription_records
            WHERE user_uid = ? AND verified = 1
            ORDER BY updated_at DESC
            LIMIT 1
            """,
            user.uid,
        )
        if row is None:
            return user.subscription_tier

        expires_at = _parse_dt(row.get("expires_at"))
        if expires_at:
            if expires_at.tzinfo is None:
                expires_at = expires_at.replace(tzinfo=timezone.utc)
            if expires_at < datetime.now(timezone.utc):
                await db.execute(
                    "UPDATE users SET subscription_tier = 'FREE', updated_at = datetime('now') WHERE uid = ?",
                    user.uid,
                )
                user.subscription_tier = "FREE"
                return "FREE"

        user.subscription_tier = row["tier"]
        await db.execute(
            "UPDATE users SET subscription_tier = ?, updated_at = datetime('now') WHERE uid = ?",
            row["tier"],
            user.uid,
        )
        return row["tier"]
