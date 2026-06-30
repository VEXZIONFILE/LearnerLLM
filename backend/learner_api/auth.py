from dataclasses import dataclass

from fastapi import Depends, HTTPException, Request, status
from firebase_admin import auth as firebase_auth
from firebase_admin import credentials, initialize_app
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.config import Settings, get_settings
from learner_api.database import get_db
from learner_api.models import User

_firebase_initialized = False


def _ensure_firebase(settings: Settings) -> None:
    global _firebase_initialized
    if _firebase_initialized or settings.firebase_auth_disabled:
        return
    if settings.firebase_credentials_path:
        cred = credentials.Certificate(settings.firebase_credentials_path)
        initialize_app(cred, {"projectId": settings.firebase_project_id or None})
    else:
        initialize_app(options={"projectId": settings.firebase_project_id or None})
    _firebase_initialized = True


@dataclass
class AuthUser:
    uid: str
    email: str
    display_name: str
    photo_url: str | None


async def verify_token(request: Request, settings: Settings = Depends(get_settings)) -> AuthUser:
    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")

    token = auth_header.removeprefix("Bearer ").strip()
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")

    if settings.firebase_auth_disabled:
        uid = token if token != "dev" else settings.dev_auth_uid
        return AuthUser(uid=uid, email=f"{uid}@dev.local", display_name="Dev Student", photo_url=None)

    _ensure_firebase(settings)
    try:
        decoded = firebase_auth.verify_id_token(token)
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid Firebase token") from exc

    return AuthUser(
        uid=decoded["uid"],
        email=decoded.get("email", ""),
        display_name=decoded.get("name") or decoded.get("email", "Student").split("@")[0],
        photo_url=decoded.get("picture"),
    )


async def get_current_user(
    auth_user: AuthUser = Depends(verify_token),
    db: AsyncSession = Depends(get_db),
) -> User:
    result = await db.execute(select(User).where(User.uid == auth_user.uid))
    user = result.scalar_one_or_none()
    if user is None:
        user = User(
            uid=auth_user.uid,
            email=auth_user.email,
            display_name=auth_user.display_name,
            photo_url=auth_user.photo_url,
        )
        db.add(user)
        await db.commit()
        await db.refresh(user)
    return user
