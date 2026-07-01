import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_report_content(client: AsyncClient, auth_headers: dict[str, str]):
    await client.get("/v1/me", headers=auth_headers)

    response = await client.post(
        "/v1/reports",
        headers=auth_headers,
        json={
            "session_id": "session-123",
            "message_id": 42,
            "content": "This AI reply was inappropriate.",
            "reason": "OFFENSIVE",
            "details": "Used harsh language.",
            "app_mode": "TUTOR",
        },
    )
    assert response.status_code == 201
    data = response.json()
    assert data["status"] == "received"
    assert isinstance(data["report_id"], int)
