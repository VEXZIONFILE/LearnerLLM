from fastapi import APIRouter, Depends

from learner_api.auth import get_current_user, get_settings
from learner_api.config import Settings
from learner_api.database import D1Database, get_db
from learner_api.models import User
from learner_api.schemas import RecordScanRequest, ScanQuotaResponse
from learner_api.services.scan_quota import ScanQuotaService

router = APIRouter(prefix="/v1/scans", tags=["scans"])


@router.get("/quota", response_model=ScanQuotaResponse)
async def get_scan_quota(
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> ScanQuotaResponse:
    service = ScanQuotaService(settings)
    return await service.get_status(db, user)


@router.post("", response_model=ScanQuotaResponse)
async def record_scan(
    body: RecordScanRequest | None = None,
    user: User = Depends(get_current_user),
    db: D1Database = Depends(get_db),
    settings: Settings = Depends(get_settings),
) -> ScanQuotaResponse:
    _ = body
    service = ScanQuotaService(settings)
    return await service.record_scan(db, user)
