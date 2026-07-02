import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_message_quota_free_tier(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.get(
        "/v1/chat/quota",
        headers=auth_headers,
        params={"app_mode": "TUTOR", "free_model_variant": "TUTOR"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["daily_limit"] == 60
    assert data["can_send"] is True
    assert data["app_mode"] == "TUTOR"
    assert data["free_model_variant"] == "TUTOR"
    assert "GPT-OSS" in data["quota_label"]


@pytest.mark.asyncio
async def test_message_quota_shared_across_modes(client: AsyncClient, auth_headers: dict[str, str]):
    for _ in range(60):
        response = await client.post(
            "/v1/chat/messages",
            headers=auth_headers,
            json={
                "grade_level": 8,
                "app_mode": "TUTOR",
                "free_model_variant": "TUTOR",
                "student_message": "Help me with fractions",
                "subject": {"kind": "builtin", "builtin": "MATH"},
            },
        )
        assert response.status_code == 200

    quota_tutor = await client.get(
        "/v1/chat/quota",
        headers=auth_headers,
        params={"app_mode": "TUTOR", "free_model_variant": "TUTOR"},
    )
    assert quota_tutor.json()["can_send"] is False

    quota_study_mode = await client.get(
        "/v1/chat/quota",
        headers=auth_headers,
        params={"app_mode": "STUDY", "free_model_variant": "TUTOR"},
    )
    assert quota_study_mode.json()["can_send"] is False

    quota_other_model = await client.get(
        "/v1/chat/quota",
        headers=auth_headers,
        params={"app_mode": "STUDY", "free_model_variant": "STUDY"},
    )
    assert quota_other_model.json()["can_send"] is True
