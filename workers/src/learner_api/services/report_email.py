import logging

from learner_api.config import Settings
from learner_api.http_client import post_json
from learner_api.models import User
from learner_api.schemas import ReportContentRequest

logger = logging.getLogger(__name__)


def _build_email_body(
    report_id: int,
    user: User,
    body: ReportContentRequest,
) -> tuple[str, str]:
    subject = f"[LearnerLM] AI content report #{report_id} — {body.reason.value}"
    details = body.details.strip() if body.details else "(none)"
    text = f"""New AI content report received

Report ID: {report_id}
User UID: {user.uid}
User email: {user.email or "(unknown)"}
Session ID: {body.session_id or "(none)"}
Message ID: {body.message_id or "(none)"}
App mode: {body.app_mode or "(none)"}
Reason: {body.reason.value}

Reported content:
{body.content}

Additional details:
{details}
"""
    return subject, text


async def send_report_notification(
    settings: Settings,
    report_id: int,
    user: User,
    body: ReportContentRequest,
) -> None:
    if settings.report_email_disabled:
        return

    to_email = settings.report_notification_email.strip()
    api_key = settings.resend_api_key.strip()
    if not to_email or not api_key:
        logger.warning(
            "Report #%s saved but email not sent: set REPORT_NOTIFICATION_EMAIL and RESEND_API_KEY",
            report_id,
        )
        return

    subject, text = _build_email_body(report_id, user, body)
    from_email = settings.report_email_from.strip() or "LearnerLM Reports <onboarding@resend.dev>"

    try:
        await post_json(
            "https://api.resend.com/emails",
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
            },
            payload={
                "from": from_email,
                "to": [to_email],
                "subject": subject,
                "text": text,
            },
        )
    except Exception:
        logger.exception("Failed to send report notification email for report #%s", report_id)
