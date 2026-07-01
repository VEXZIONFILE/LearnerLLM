import uuid

from fastapi import APIRouter, Depends

from learner_api.auth import get_current_user, get_settings
from learner_api.config import Settings
from learner_api.database import D1Database, _parse_dt, get_db
from learner_api.models import User
from learner_api.schemas import (
    ChatMessageResponse,
    ChatRequest,
    ChatResponse,
    ChatSessionResponse,
)
from learner_api.services.billing import BillingService
from learner_api.services.openrouter import OpenRouterClient
from learner_api.services.progress import record_study_activity, resolve_subject_fields, update_streak
from learner_api.services.prompt_builder import TutorContext
from learner_api.services.tutor_engine import TutorEngine

router = APIRouter(prefix="/v1/chat", tags=["chat"])


def _get_engine(settings: Settings) -> TutorEngine:
    return TutorEngine(openrouter=OpenRouterClient(settings))


@router.post("/messages", response_model=ChatResponse)
async def send_message(
    body: ChatRequest,
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> ChatResponse:
    billing = BillingService(settings)
    tier = await billing.refresh_user_tier(db, user)

    session_id = body.session_id or str(uuid.uuid4())
    session = await db.fetchone(
        "SELECT id FROM chat_sessions WHERE id = ? AND user_uid = ?",
        session_id,
        user.uid,
    )
    if session is None:
        await db.execute(
            "INSERT INTO chat_sessions (id, user_uid, title) VALUES (?, ?, ?)",
            session_id,
            user.uid,
            body.student_message[:80],
        )

    subject_key, subject_display, subject_category = await resolve_subject_fields(db, user, body.subject)
    history = [(msg.role, msg.content) for msg in body.conversation_history]

    await db.execute(
        """
        INSERT INTO chat_messages (session_id, role, content, subject_key, hint_level)
        VALUES (?, 'student', ?, ?, ?)
        """,
        session_id,
        body.student_message,
        subject_key,
        body.hint_level.value,
    )

    engine = _get_engine(settings)
    context = TutorContext(
        grade_level=body.grade_level,
        subject_key=subject_key,
        subject_display_name=subject_display,
        subject_category=subject_category,
        app_mode=body.app_mode,
        subscription_tier=tier,
        hint_level=body.hint_level,
        student_message=body.student_message,
        conversation_history=history,
        scanned_text=body.scanned_text,
    )
    response = await engine.respond(context, tier)

    await db.execute(
        """
        INSERT INTO chat_messages (session_id, role, content, subject_key, hint_level)
        VALUES (?, 'tutor', ?, ?, ?)
        """,
        session_id,
        response.message,
        response.subject_key,
        response.hint_level.value,
    )
    await db.execute(
        "UPDATE chat_sessions SET updated_at = datetime('now') WHERE id = ?",
        session_id,
    )

    await record_study_activity(db, user, response.subject_key, response.subject_display_name)
    await update_streak(db, user)

    return ChatResponse(
        session_id=session_id,
        message=response.message,
        hint_level=response.hint_level,
        subject_key=response.subject_key,
        subject_display_name=response.subject_display_name,
        model_label=response.model_label,
        detected_mistake=response.detected_mistake,
        encourages_attempt=response.encourages_attempt,
    )


@router.get("/sessions", response_model=list[ChatSessionResponse])
async def list_sessions(
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
) -> list[ChatSessionResponse]:
    rows = await db.fetchall(
        """
        SELECT * FROM chat_sessions
        WHERE user_uid = ?
        ORDER BY updated_at DESC
        """,
        user.uid,
    )
    return [
        ChatSessionResponse(
            id=row["id"],
            title=row["title"],
            created_at=_parse_dt(row["created_at"]),
            updated_at=_parse_dt(row["updated_at"]),
        )
        for row in rows
    ]


@router.get("/sessions/{session_id}/messages", response_model=list[ChatMessageResponse])
async def list_messages(
    session_id: str,
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
) -> list[ChatMessageResponse]:
    session = await db.fetchone(
        "SELECT id FROM chat_sessions WHERE id = ? AND user_uid = ?",
        session_id,
        user.uid,
    )
    if session is None:
        return []

    rows = await db.fetchall(
        """
        SELECT * FROM chat_messages
        WHERE session_id = ?
        ORDER BY created_at ASC
        """,
        session_id,
    )
    return [
        ChatMessageResponse(
            id=int(row["id"]),
            role=row["role"],
            content=row["content"],
            subject_key=row["subject_key"],
            hint_level=int(row["hint_level"]),
            created_at=_parse_dt(row["created_at"]),
        )
        for row in rows
    ]
