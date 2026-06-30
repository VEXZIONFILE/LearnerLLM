from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from learner_api.auth import get_current_user
from learner_api.database import get_db
from learner_api.models import CustomSubject, User
from learner_api.schemas import CreateCustomSubjectRequest, CustomSubjectResponse, SubjectCategory

router = APIRouter(prefix="/v1/subjects", tags=["subjects"])

CATEGORY_EMOJI = {
    SubjectCategory.CLASS: "🏫",
    SubjectCategory.AFTER_SCHOOL: "🎨",
    SubjectCategory.PROJECT: "📋",
    SubjectCategory.CLUB: "⚽",
    SubjectCategory.EXAM_PREP: "📝",
    SubjectCategory.OTHER: "✨",
}


def _to_response(subject: CustomSubject) -> CustomSubjectResponse:
    return CustomSubjectResponse(
        id=subject.id,
        name=subject.name,
        category=SubjectCategory(subject.category),
        emoji=subject.emoji,
        storage_key=f"custom:{subject.id}",
    )


@router.get("", response_model=list[CustomSubjectResponse])
async def list_subjects(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> list[CustomSubjectResponse]:
    result = await db.execute(
        select(CustomSubject).where(CustomSubject.user_uid == user.uid).order_by(CustomSubject.created_at.asc())
    )
    return [_to_response(s) for s in result.scalars().all()]


@router.post("", response_model=CustomSubjectResponse, status_code=status.HTTP_201_CREATED)
async def create_subject(
    body: CreateCustomSubjectRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> CustomSubjectResponse:
    name = body.name.strip()
    if not name:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Subject name cannot be empty")

    subject = CustomSubject(
        user_uid=user.uid,
        name=name,
        category=body.category.value,
        emoji=body.emoji or CATEGORY_EMOJI.get(body.category, "✨"),
    )
    db.add(subject)
    await db.commit()
    await db.refresh(subject)
    return _to_response(subject)


@router.delete("/{subject_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_subject(
    subject_id: int,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> None:
    result = await db.execute(
        select(CustomSubject).where(CustomSubject.id == subject_id, CustomSubject.user_uid == user.uid)
    )
    subject = result.scalar_one_or_none()
    if subject is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Subject not found")
    await db.delete(subject)
    await db.commit()
