import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_chat_message_offline_fallback(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.post(
        "/v1/chat/messages",
        headers=auth_headers,
        json={
            "grade_level": 8,
            "app_mode": "TUTOR",
            "student_message": "How do I solve 2x + 4 = 10?",
            "subject": {"kind": "builtin", "builtin": "MATH"},
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["session_id"]
    assert "step" in data["message"].lower() or "know" in data["message"].lower()
    assert data["model_label"] == "GPT-OSS"


@pytest.mark.asyncio
async def test_chat_free_mode_study_variant(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.post(
        "/v1/chat/messages",
        headers=auth_headers,
        json={
            "grade_level": 9,
            "app_mode": "STUDY",
            "free_model_variant": "STUDY",
            "student_message": "Summarize the water cycle",
            "subject": {"kind": "builtin", "builtin": "SCIENCE"},
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["model_label"] == "Nemotron"


@pytest.mark.asyncio
async def test_chat_tutor_mode_with_laguna_variant(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.post(
        "/v1/chat/messages",
        headers=auth_headers,
        json={
            "grade_level": 9,
            "app_mode": "TUTOR",
            "free_model_variant": "CODE",
            "student_message": "Explain loops in Python",
            "subject": {"kind": "builtin", "builtin": "GENERAL"},
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["model_label"] == "Laguna"
