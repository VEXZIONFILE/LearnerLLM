from fastapi import APIRouter, Depends, HTTPException, status

from learner_api.auth import get_current_user
from learner_api.database import D1Database, get_db
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
    db: D1Database = Depends(get_db),
) -> list[CustomSubjectResponse]:
    rows = await db.fetchall(
        "SELECT * FROM custom_subjects WHERE user_uid = ? ORDER BY created_at ASC",
        user.uid,
    )
    return [
        _to_response(
            CustomSubject(
                id=int(row["id"]),
                user_uid=row["user_uid"],
                name=row["name"],
                category=row["category"],
                emoji=row["emoji"],
            )
        )
        for row in rows
    ]


@router.post("", response_model=CustomSubjectResponse, status_code=status.HTTP_201_CREATED)
async def create_subject(
    body: CreateCustomSubjectRequest,
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
) -> CustomSubjectResponse:
    name = body.name.strip()
    if not name:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Subject name cannot be empty")

    emoji = body.emoji or CATEGORY_EMOJI.get(body.category, "✨")
    subject_id = await db.insert_returning_id(
        """
        INSERT INTO custom_subjects (user_uid, name, category, emoji)
        VALUES (?, ?, ?, ?)
        """,
        user.uid,
        name,
        body.category.value,
        emoji,
    )
    return _to_response(
        CustomSubject(
            id=subject_id,
            user_uid=user.uid,
            name=name,
            category=body.category.value,
            emoji=emoji,
        )
    )


@router.delete("/{subject_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_subject(
    subject_id: int,
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
) -> None:
    row = await db.fetchone(
        "SELECT id FROM custom_subjects WHERE id = ? AND user_uid = ?",
        subject_id,
        user.uid,
    )
    if row is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Subject not found")
    await db.execute("DELETE FROM custom_subjects WHERE id = ?", subject_id)
