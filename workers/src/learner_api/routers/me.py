from fastapi import APIRouter, Depends, Request

from learner_api.auth import get_current_user, get_settings
from learner_api.config import Settings
from learner_api.database import D1Database, _parse_dt, get_db
from learner_api.models import User
from learner_api.schemas import HealthResponse, UpdateProfileRequest, UserProfileResponse
from learner_api.services.billing import BillingService

router = APIRouter(tags=["health", "me"])


@router.get("/health", response_model=HealthResponse)
async def health(request: Request) -> HealthResponse:
    settings = get_settings(request)
    return HealthResponse(status="ok", app=settings.app_name)


@router.get("/v1/me", response_model=UserProfileResponse)
async def get_me(
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> UserProfileResponse:
    billing = BillingService(settings)
    await billing.refresh_user_tier(db, user)
    row = await db.fetchone("SELECT * FROM users WHERE uid = ?", user.uid)
    if row:
        user.subscription_tier = row["subscription_tier"]
        user.grade_level = int(row["grade_level"])
        user.display_name = row["display_name"]
        user.created_at = _parse_dt(row["created_at"])
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
    db: D1Database = Depends(get_db),
) -> UserProfileResponse:
    if body.grade_level is not None:
        await db.execute(
            "UPDATE users SET grade_level = ?, updated_at = datetime('now') WHERE uid = ?",
            body.grade_level,
            user.uid,
        )
        user.grade_level = body.grade_level
    if body.display_name is not None:
        name = body.display_name.strip()
        await db.execute(
            "UPDATE users SET display_name = ?, updated_at = datetime('now') WHERE uid = ?",
            name,
            user.uid,
        )
        user.display_name = name

    row = await db.fetchone("SELECT * FROM users WHERE uid = ?", user.uid)
    return UserProfileResponse(
        uid=user.uid,
        email=row["email"],
        display_name=row["display_name"],
        photo_url=row.get("photo_url"),
        grade_level=int(row["grade_level"]),
        subscription_tier=row["subscription_tier"],
        created_at=_parse_dt(row["created_at"]),
    )
