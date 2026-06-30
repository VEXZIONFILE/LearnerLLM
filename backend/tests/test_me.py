import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_get_me_creates_user(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.get("/v1/me", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["uid"] == "dev-user"
    assert data["subscription_tier"] == "FREE"
    assert data["grade_level"] == 8


@pytest.mark.asyncio
async def test_update_grade_level(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.patch("/v1/me", headers=auth_headers, json={"grade_level": 10})
    assert response.status_code == 200
    assert response.json()["grade_level"] == 10
