from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.auth import get_current_user
from learner_api.config import get_settings
from learner_api.database import get_db
from learner_api.models import User
from learner_api.schemas import BillingVerifyRequest, BillingVerifyResponse
from learner_api.services.billing import BillingService

router = APIRouter(prefix="/v1/billing", tags=["billing"])


@router.post("/verify", response_model=BillingVerifyResponse)
async def verify_purchase(
    body: BillingVerifyRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> BillingVerifyResponse:
    settings = get_settings()
    service = BillingService(settings)
    return await service.verify_purchase(
        db=db,
        user=user,
        product_id=body.product_id,
        purchase_token=body.purchase_token,
        package_name=body.package_name or settings.google_play_package_name,
    )
