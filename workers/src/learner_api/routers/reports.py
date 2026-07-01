from fastapi import APIRouter, Depends, status

from learner_api.auth import get_current_user, get_settings
from learner_api.config import Settings
from learner_api.database import D1Database, get_db
from learner_api.models import User
from learner_api.schemas import ReportContentRequest, ReportContentResponse

router = APIRouter(prefix="/v1/reports", tags=["reports"])


@router.post("", response_model=ReportContentResponse, status_code=status.HTTP_201_CREATED)
async def report_content(
    body: ReportContentRequest,
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> ReportContentResponse:
    _ = settings
    report_id = await db.insert_returning_id(
        """
        INSERT INTO content_reports
        (user_uid, session_id, message_id, content, reason, details, app_mode, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, 'open')
        """,
        user.uid,
        body.session_id,
        body.message_id,
        body.content,
        body.reason.value,
        body.details,
        body.app_mode,
    )
    return ReportContentResponse(report_id=report_id, status="received")
