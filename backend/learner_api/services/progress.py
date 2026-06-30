from datetime import date, timedelta

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.models import CustomSubject, LearningStreak, StudyTopic, User
from learner_api.schemas import (
    BuiltinSubject,
    LearningStreakResponse,
    ProgressResponse,
    StudyTopicResponse,
    SubjectCategory,
)
from learner_api.services.prompt_builder import CATEGORY_LABELS


async def resolve_subject_fields(
    db: AsyncSession,
    user: User,
    subject_input,
) -> tuple[str, str, str | None]:
    if subject_input is None:
        return "builtin:GENERAL", "General", None

    if subject_input.kind == "custom" and subject_input.custom_id is not None:
        result = await db.execute(
            select(CustomSubject).where(
                CustomSubject.id == subject_input.custom_id,
                CustomSubject.user_uid == user.uid,
            )
        )
        custom = result.scalar_one_or_none()
        if custom is None:
            return "builtin:GENERAL", "General", None
        category = SubjectCategory(custom.category)
        return (
            f"custom:{custom.id}",
            custom.name,
            CATEGORY_LABELS.get(category, custom.category),
        )

    builtin = subject_input.builtin or BuiltinSubject.GENERAL
    return f"builtin:{builtin.value}", builtin.value.title(), None


async def record_study_activity(db: AsyncSession, user: User, subject_key: str, name: str) -> None:
    result = await db.execute(
        select(StudyTopic).where(StudyTopic.user_uid == user.uid, StudyTopic.subject_key == subject_key)
    )
    topic = result.scalar_one_or_none()
    if topic is None:
        topic = StudyTopic(user_uid=user.uid, name=name, subject_key=subject_key, strength_score=0.4)
        db.add(topic)
    else:
        topic.name = name
        topic.strength_score = 0.4
    await db.commit()


async def update_streak(db: AsyncSession, user: User) -> LearningStreak:
    today = date.today().isoformat()
    result = await db.execute(select(LearningStreak).where(LearningStreak.user_uid == user.uid))
    streak = result.scalar_one_or_none()
    if streak is None:
        streak = LearningStreak(user_uid=user.uid)
        db.add(streak)

    if streak.last_active_date == today:
        new_streak = streak.current_streak
    elif streak.last_active_date == (date.today() - timedelta(days=1)).isoformat():
        new_streak = streak.current_streak + 1
    else:
        new_streak = 1

    streak.current_streak = new_streak
    streak.longest_streak = max(new_streak, streak.longest_streak or 0)
    streak.last_active_date = today
    await db.commit()
    await db.refresh(streak)
    return streak


async def get_progress(db: AsyncSession, user: User) -> ProgressResponse:
    topics_result = await db.execute(
        select(StudyTopic).where(StudyTopic.user_uid == user.uid).order_by(StudyTopic.last_studied_at.desc())
    )
    topics = topics_result.scalars().all()
    streak_result = await db.execute(select(LearningStreak).where(LearningStreak.user_uid == user.uid))
    streak = streak_result.scalar_one_or_none() or LearningStreak(user_uid=user.uid)

    topic_responses = [
        StudyTopicResponse(
            id=t.id,
            name=t.name,
            subject_key=t.subject_key,
            strength_score=t.strength_score,
            last_studied_at=t.last_studied_at,
        )
        for t in topics
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
