from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.auth import get_current_user
from learner_api.config import get_settings
from learner_api.database import get_db
from learner_api.models import User
from learner_api.schemas import HealthResponse, UpdateProfileRequest, UserProfileResponse
from learner_api.services.billing import BillingService

router = APIRouter(tags=["health", "me"])


@router.get("/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    settings = get_settings()
    return HealthResponse(status="ok", app=settings.app_name)


@router.get("/v1/me", response_model=UserProfileResponse)
async def get_me(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> UserProfileResponse:
    billing = BillingService(get_settings())
    await billing.refresh_user_tier(db, user)
    await db.refresh(user)
    return UserProfileResponse(
        uid=user.uid,
        email=user.email,
        display_name=user.display_name,
        photo_url=user.photo_url,
        grade_level=user.grade_level,
        subscription_tier=user.subscription_tier,
        created_at=user.created_at,
    )


@router.patch("/v1/me", response_model=UserProfileResponse)
async def update_me(
    body: UpdateProfileRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> UserProfileResponse:
    if body.grade_level is not None:
        user.grade_level = body.grade_level
    if body.display_name is not None:
        user.display_name = body.display_name.strip()
    await db.commit()
    await db.refresh(user)
    return UserProfileResponse(
        uid=user.uid,
        email=user.email,
        display_name=user.display_name,
        photo_url=user.photo_url,
        grade_level=user.grade_level,
        subscription_tier=user.subscription_tier,
        created_at=user.created_at,
    )
