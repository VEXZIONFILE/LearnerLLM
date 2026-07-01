from datetime import date, datetime, timedelta, timezone

from learner_api.database import D1Database, _parse_dt
from learner_api.models import LearningStreak, User
from learner_api.schemas import (
    BuiltinSubject,
    LearningStreakResponse,
    ProgressResponse,
    StudyTopicResponse,
    SubjectCategory,
)
from learner_api.services.prompt_builder import CATEGORY_LABELS


async def resolve_subject_fields(
    db: D1Database,
    user: User,
    subject_input,
) -> tuple[str, str, str | None]:
    if subject_input is None:
        return "builtin:GENERAL", "General", None

    if subject_input.kind == "custom" and subject_input.custom_id is not None:
        row = await db.fetchone(
            "SELECT * FROM custom_subjects WHERE id = ? AND user_uid = ?",
            subject_input.custom_id,
            user.uid,
        )
        if row is None:
            return "builtin:GENERAL", "General", None
        category = SubjectCategory(row["category"])
        return (
            f"custom:{row['id']}",
            row["name"],
            CATEGORY_LABELS.get(category, row["category"]),
        )

    builtin = subject_input.builtin or BuiltinSubject.GENERAL
    return f"builtin:{builtin.value}", builtin.value.title(), None


async def record_study_activity(db: D1Database, user: User, subject_key: str, name: str) -> None:
    row = await db.fetchone(
        "SELECT id FROM study_topics WHERE user_uid = ? AND subject_key = ?",
        user.uid,
        subject_key,
    )
    if row is None:
        await db.execute(
            """
            INSERT INTO study_topics (user_uid, name, subject_key, strength_score, last_studied_at)
            VALUES (?, ?, ?, 0.4, datetime('now'))
            """,
            user.uid,
            name,
            subject_key,
        )
    else:
        await db.execute(
            """
            UPDATE study_topics
            SET name = ?, strength_score = 0.4, last_studied_at = datetime('now')
            WHERE user_uid = ? AND subject_key = ?
            """,
            name,
            user.uid,
            subject_key,
        )


async def update_streak(db: D1Database, user: User) -> LearningStreak:
    today = date.today().isoformat()
    row = await db.fetchone("SELECT * FROM learning_streaks WHERE user_uid = ?", user.uid)
    if row is None:
        await db.execute(
            """
            INSERT INTO learning_streaks (user_uid, current_streak, longest_streak, last_active_date)
            VALUES (?, 0, 0, '')
            """,
            user.uid,
        )
        row = {"user_uid": user.uid, "current_streak": 0, "longest_streak": 0, "last_active_date": ""}

    if row["last_active_date"] == today:
        new_streak = int(row["current_streak"])
    elif row["last_active_date"] == (date.today() - timedelta(days=1)).isoformat():
        new_streak = int(row["current_streak"]) + 1
    else:
        new_streak = 1

    longest = max(new_streak, int(row.get("longest_streak") or 0))
    await db.execute(
        """
        UPDATE learning_streaks
        SET current_streak = ?, longest_streak = ?, last_active_date = ?
        WHERE user_uid = ?
        """,
        new_streak,
        longest,
        today,
        user.uid,
    )
    return LearningStreak(
        user_uid=user.uid,
        current_streak=new_streak,
        longest_streak=longest,
        last_active_date=today,
    )


async def get_progress(db: D1Database, user: User) -> ProgressResponse:
    topic_rows = await db.fetchall(
        """
        SELECT * FROM study_topics
        WHERE user_uid = ?
        ORDER BY last_studied_at DESC
        """,
        user.uid,
    )
    streak_row = await db.fetchone("SELECT * FROM learning_streaks WHERE user_uid = ?", user.uid)
    streak = LearningStreak(
        user_uid=user.uid,
        current_streak=int(streak_row["current_streak"]) if streak_row else 0,
        longest_streak=int(streak_row["longest_streak"]) if streak_row else 0,
        last_active_date=streak_row["last_active_date"] if streak_row else "",
    )

    topic_responses = [
        StudyTopicResponse(
            id=int(row["id"]),
            name=row["name"],
            subject_key=row["subject_key"],
            strength_score=float(row["strength_score"]),
            last_studied_at=_parse_dt(row["last_studied_at"]) or datetime.now(timezone.utc),
        )
        for row in topic_rows
    ]
    weak = [t for t in topic_responses if t.strength_score < 0.5][:5]

    return ProgressResponse(
        topics=topic_responses,
        streak=LearningStreakResponse(
            current_streak=streak.current_streak,
            longest_streak=streak.longest_streak,
            last_active_date=streak.last_active_date,
        ),
        weak_topics=weak,
    )
