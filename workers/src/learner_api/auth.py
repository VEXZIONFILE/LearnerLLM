from dataclasses import dataclass
from datetime import datetime

from fastapi import Depends, HTTPException, Request, status

from learner_api.config import Settings, get_settings_from_request
from learner_api.database import D1Database, _parse_dt, get_db
from learner_api.firebase_auth import verify_firebase_id_token
from learner_api.models import User


@dataclass
class AuthUser:
    uid: str
    email: str
    display_name: str
    photo_url: str | None


def _row_to_user(row: dict) -> User:
    return User(
        uid=row["uid"],
        email=row.get("email", ""),
        display_name=row.get("display_name", "Student"),
        photo_url=row.get("photo_url"),
        grade_level=int(row.get("grade_level", 8)),
        subscription_tier=row.get("subscription_tier", "FREE"),
        created_at=_parse_dt(row.get("created_at")),
        updated_at=_parse_dt(row.get("updated_at")),
    )


async def verify_token(request: Request) -> AuthUser:
    settings = get_settings_from_request(request)
    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")

    token = auth_header.removeprefix("Bearer ").strip()
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")

    if settings.firebase_auth_disabled:
        uid = token if token != "dev" else settings.dev_auth_uid
        return AuthUser(uid=uid, email=f"{uid}@dev.local", display_name="Dev Student", photo_url=None)

    try:
        decoded = await verify_firebase_id_token(token, settings.firebase_project_id)
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid Firebase token") from exc

    return AuthUser(
        uid=decoded["uid"],
        email=decoded.get("email", ""),
        display_name=decoded.get("name") or decoded.get("email", "Student").split("@")[0],
        photo_url=decoded.get("picture"),
    )


async def get_current_user(
    request: Request,
    auth_user: AuthUser = Depends(verify_token),
    db: D1Database = Depends(get_db),
) -> User:
    row = await db.fetchone("SELECT * FROM users WHERE uid = ?", auth_user.uid)
    if row is not None:
        return _row_to_user(row)

    await db.execute(
        """
        INSERT INTO users (uid, email, display_name, photo_url)
        VALUES (?, ?, ?, ?)
        """,
        auth_user.uid,
        auth_user.email,
        auth_user.display_name,
        auth_user.photo_url,
    )
    row = await db.fetchone("SELECT * FROM users WHERE uid = ?", auth_user.uid)
    return _row_to_user(row)


def get_settings(request: Request) -> Settings:
    return get_settings_from_request(request)
