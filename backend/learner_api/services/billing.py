from datetime import datetime, timedelta, timezone

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.config import Settings
from learner_api.models import SubscriptionRecord, User
from learner_api.schemas import BillingVerifyResponse


PRODUCT_TIER_MAP = {
    "learnerlm_basic_monthly": "BASIC",
    "learnerlm_pro_yearly": "BASIC",
    "learnerlm_pro_monthly": "PRO",
}

MEGA_PRODUCT_ID = "learnerlm_pro_yearly"


class BillingService:
    def __init__(self, settings: Settings) -> None:
        self.settings = settings

    async def verify_purchase(
        self,
        db: AsyncSession,
        user: User,
        product_id: str,
        purchase_token: str,
        package_name: str | None,
    ) -> BillingVerifyResponse:
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
            user.subscription_tier = tier
            record = SubscriptionRecord(
                user_uid=user.uid,
                product_id=product_id,
                purchase_token=purchase_token,
                tier=tier,
                verified=True,
                expires_at=expires_at,
            )
            db.add(record)
            await db.commit()
            await db.refresh(user)

        return BillingVerifyResponse(
            verified=verified,
            subscription_tier=user.subscription_tier,
            product_id=product_id,
        )

    async def refresh_user_tier(self, db: AsyncSession, user: User) -> str:
        result = await db.execute(
            select(SubscriptionRecord)
            .where(SubscriptionRecord.user_uid == user.uid, SubscriptionRecord.verified.is_(True))
            .order_by(SubscriptionRecord.updated_at.desc())
        )
        record = result.scalars().first()
        if record is None:
            return user.subscription_tier

        if record.expires_at:
            expires = record.expires_at
            if expires.tzinfo is None:
                expires = expires.replace(tzinfo=timezone.utc)
            if expires < datetime.now(timezone.utc):
                user.subscription_tier = "FREE"
                await db.commit()
                return "FREE"

        user.subscription_tier = record.tier
        await db.commit()
        return record.tier

    async def get_active_product_id(self, db: AsyncSession, user: User) -> str | None:
        result = await db.execute(
            select(SubscriptionRecord)
            .where(SubscriptionRecord.user_uid == user.uid, SubscriptionRecord.verified.is_(True))
            .order_by(SubscriptionRecord.updated_at.desc())
        )
        record = result.scalars().first()
        if record is None:
            return None

        if record.expires_at:
            expires = record.expires_at
            if expires.tzinfo is None:
                expires = expires.replace(tzinfo=timezone.utc)
            if expires < datetime.now(timezone.utc):
                return None

        return record.product_id
