import asyncio
import logging
import smtplib
from email.message import EmailMessage

import httpx

from learner_api.config import Settings
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


async def _send_via_resend(
    settings: Settings,
    to_email: str,
    subject: str,
    text: str,
) -> None:
    api_key = settings.resend_api_key.strip()
    if not api_key:
        return

    from_email = settings.report_email_from.strip() or "LearnerLM Reports <onboarding@resend.dev>"
    async with httpx.AsyncClient(timeout=15.0) as client:
        response = await client.post(
            "https://api.resend.com/emails",
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
            },
            json={
                "from": from_email,
                "to": [to_email],
                "subject": subject,
                "text": text,
            },
        )
        response.raise_for_status()


def _send_via_smtp_sync(
    settings: Settings,
    to_email: str,
    subject: str,
    text: str,
) -> None:
    host = settings.smtp_host.strip()
    user = settings.smtp_user.strip()
    password = settings.smtp_password.strip()
    if not host or not user or not password:
        return

    from_email = settings.report_email_from.strip() or user
    message = EmailMessage()
    message["From"] = from_email
    message["To"] = to_email
    message["Subject"] = subject
    message.set_content(text)

    with smtplib.SMTP(host, settings.smtp_port, timeout=20) as smtp:
        if settings.smtp_use_tls:
            smtp.starttls()
        smtp.login(user, password)
        smtp.send_message(message)


async def send_report_notification(
    settings: Settings,
    report_id: int,
    user: User,
    body: ReportContentRequest,
) -> None:
    if settings.report_email_disabled:
        return

    to_email = settings.report_notification_email.strip()
    if not to_email:
        return

    subject, text = _build_email_body(report_id, user, body)

    try:
        if settings.resend_api_key.strip():
            await _send_via_resend(settings, to_email, subject, text)
            return
        if settings.smtp_host.strip():
            await asyncio.to_thread(_send_via_smtp_sync, settings, to_email, subject, text)
            return
        logger.warning(
            "Report #%s saved but email not sent: configure RESEND_API_KEY or SMTP_*",
            report_id,
        )
    except Exception:
        logger.exception("Failed to send report notification email for report #%s", report_id)
