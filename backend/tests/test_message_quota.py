import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_message_quota_free_tier(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.get(
        "/v1/chat/quota",
        headers=auth_headers,
        params={"app_mode": "TUTOR"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["daily_limit"] == 60
    assert data["can_send"] is True
    assert data["app_mode"] == "TUTOR"
    assert "Tutor" in data["quota_label"]
