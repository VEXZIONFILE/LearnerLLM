import json
from typing import Any

from workers import fetch


async def post_json(url: str, headers: dict[str, str], payload: dict[str, Any]) -> dict[str, Any]:
    response = await fetch(
        url,
        method="POST",
        headers=headers,
        body=json.dumps(payload),
    )
    if response.status >= 400:
        raise RuntimeError(f"HTTP {response.status}")
    return await response.json()


async def get_json(url: str) -> dict[str, Any]:
    response = await fetch(url, method="GET")
    if response.status >= 400:
        raise RuntimeError(f"HTTP {response.status}")
    return await response.json()
