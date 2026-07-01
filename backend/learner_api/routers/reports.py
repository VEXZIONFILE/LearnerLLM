from fastapi import APIRouter, Depends, status

from learner_api.auth import get_current_user
from learner_api.config import Settings, get_settings
from learner_api.database import get_db
from learner_api.models import ContentReport, User
from learner_api.schemas import ReportContentRequest, ReportContentResponse
from learner_api.services.report_email import send_report_notification
from sqlalchemy.ext.asyncio import AsyncSession

router = APIRouter(prefix="/v1/reports", tags=["reports"])


@router.post("", response_model=ReportContentResponse, status_code=status.HTTP_201_CREATED)
async def report_content(
    body: ReportContentRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> ReportContentResponse:
    report = ContentReport(
        user_uid=user.uid,
        session_id=body.session_id,
        message_id=body.message_id,
        content=body.content,
        reason=body.reason.value,
        details=body.details,
        app_mode=body.app_mode,
        status="open",
    )
    db.add(report)
    await db.commit()
    await db.refresh(report)
    await send_report_notification(settings, report.id, user, body)
    return ReportContentResponse(report_id=report.id, status="received")
