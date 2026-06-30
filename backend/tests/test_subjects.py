import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_custom_subject_crud(client: AsyncClient, auth_headers: dict[str, str]):
    create = await client.post(
        "/v1/subjects",
        headers=auth_headers,
        json={"name": "Robotics Club", "category": "CLUB"},
    )
    assert create.status_code == 201
    subject_id = create.json()["id"]

    listing = await client.get("/v1/subjects", headers=auth_headers)
    assert listing.status_code == 200
    assert len(listing.json()) == 1

    delete = await client.delete(f"/v1/subjects/{subject_id}", headers=auth_headers)
    assert delete.status_code == 204

    listing_after = await client.get("/v1/subjects", headers=auth_headers)
    assert listing_after.json() == []


@pytest.mark.asyncio
async def test_billing_verify(client: AsyncClient, auth_headers: dict[str, str]):
    response = await client.post(
        "/v1/billing/verify",
        headers=auth_headers,
        json={
            "product_id": "learnerlm_basic_monthly",
            "purchase_token": "test-token",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["verified"] is True
    assert data["subscription_tier"] == "BASIC"

    me = await client.get("/v1/me", headers=auth_headers)
    assert me.json()["subscription_tier"] == "BASIC"
