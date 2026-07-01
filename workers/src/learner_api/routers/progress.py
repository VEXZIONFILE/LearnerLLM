from fastapi import APIRouter, Depends

from learner_api.auth import get_current_user
from learner_api.database import D1Database, get_db
from learner_api.models import User
from learner_api.schemas import ProgressResponse
from learner_api.services.progress import get_progress

router = APIRouter(prefix="/v1/progress", tags=["progress"])


@router.get("", response_model=ProgressResponse)
async def read_progress(
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
) -> ProgressResponse:
    return await get_progress(db, user)
