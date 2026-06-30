import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_scan_quota_free_tier(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.get("/v1/scans/quota", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["is_premium"] is False
    assert data["daily_limit"] == 3
    assert data["can_scan"] is True


@pytest.mark.asyncio
async def test_record_scan_increments(client: AsyncClient, auth_headers: dict[str, str]):
    first = await client.post("/v1/scans", headers=auth_headers, json={})
    assert first.status_code == 200
    assert first.json()["used_today"] == 1

    quota = await client.get("/v1/scans/quota", headers=auth_headers)
    assert quota.json()["remaining"] == 2
