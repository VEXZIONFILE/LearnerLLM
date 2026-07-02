import uuid

from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.auth import get_current_user
from learner_api.database import get_db
from learner_api.models import ChatMessage, ChatSession, User
from learner_api.schemas import (
    ChatMessageResponse,
    ChatRequest,
    ChatResponse,
    ChatSessionResponse,
    MessageQuotaResponse,
)
from learner_api.services.billing import BillingService
from learner_api.services.message_quota import MessageQuotaService
from learner_api.services.openrouter import OpenRouterClient
from learner_api.services.progress import record_study_activity, resolve_subject_fields, update_streak
from learner_api.services.prompt_builder import TutorContext
from learner_api.services.tutor_engine import TutorEngine
from learner_api.config import get_settings

router = APIRouter(prefix="/v1/chat", tags=["chat"])


def _get_engine() -> TutorEngine:
    settings = get_settings()
    return TutorEngine(openrouter=OpenRouterClient(settings))


@router.get("/quota", response_model=MessageQuotaResponse)
async def get_message_quota(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> MessageQuotaResponse:
    service = MessageQuotaService(get_settings())
    return await service.get_status(db, user)


@router.post("/messages", response_model=ChatResponse)
async def send_message(
    body: ChatRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> ChatResponse:
    settings = get_settings()
    billing = BillingService(settings)
    tier = await billing.refresh_user_tier(db, user)
    message_quota = MessageQuotaService(settings)
    await message_quota.ensure_can_send(db, user, tier, body.student_message)

    session_id = body.session_id or str(uuid.uuid4())
    result = await db.execute(select(ChatSession).where(ChatSession.id == session_id, ChatSession.user_uid == user.uid))
    session = result.scalar_one_or_none()
    if session is None:
        session = ChatSession(id=session_id, user_uid=user.uid, title=body.student_message[:80])
        db.add(session)
        await db.commit()

    subject_key, subject_display, subject_category = await resolve_subject_fields(db, user, body.subject)
    history = [(msg.role, msg.content) for msg in body.conversation_history]

    student_message = ChatMessage(
        session_id=session_id,
        role="student",
        content=body.student_message,
        subject_key=subject_key,
        hint_level=body.hint_level.value,
    )
    db.add(student_message)
    await db.commit()
    await message_quota.record_message(db, user)

    engine = _get_engine()
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
        free_model_variant=body.free_model_variant,
    )
    response = await engine.respond(context, tier)

    tutor_message = ChatMessage(
        session_id=session_id,
        role="tutor",
        content=response.message,
        subject_key=response.subject_key,
        hint_level=response.hint_level.value,
    )
    db.add(tutor_message)
    await db.commit()

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
    db: AsyncSession = Depends(get_db),
) -> list[ChatSessionResponse]:
    result = await db.execute(
        select(ChatSession).where(ChatSession.user_uid == user.uid).order_by(ChatSession.updated_at.desc())
    )
    sessions = result.scalars().all()
    return [
        ChatSessionResponse(
            id=s.id,
            title=s.title,
            created_at=s.created_at,
            updated_at=s.updated_at,
        )
        for s in sessions
    ]


@router.get("/sessions/{session_id}/messages", response_model=list[ChatMessageResponse])
async def list_messages(
    session_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> list[ChatMessageResponse]:
    session_result = await db.execute(
        select(ChatSession).where(ChatSession.id == session_id, ChatSession.user_uid == user.uid)
    )
    if session_result.scalar_one_or_none() is None:
        return []

    result = await db.execute(
        select(ChatMessage).where(ChatMessage.session_id == session_id).order_by(ChatMessage.created_at.asc())
    )
    messages = result.scalars().all()
    return [
        ChatMessageResponse(
            id=m.id,
            role=m.role,
            content=m.content,
            subject_key=m.subject_key,
            hint_level=m.hint_level,
            created_at=m.created_at,
        )
        for m in messages
    ]
