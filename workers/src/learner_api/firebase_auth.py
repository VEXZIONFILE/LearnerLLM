import json
import time
from typing import Any

import jwt

from learner_api.http_client import get_json

_cert_cache: dict[str, Any] = {"fetched_at": 0.0, "certs": {}}
_CERT_TTL_SECONDS = 3600


async def _get_google_certs() -> dict[str, str]:
    now = time.time()
    if _cert_cache["certs"] and now - _cert_cache["fetched_at"] < _CERT_TTL_SECONDS:
        return _cert_cache["certs"]

    certs = await get_json(
        "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
    )
    _cert_cache["certs"] = certs
    _cert_cache["fetched_at"] = now
    return certs


async def verify_firebase_id_token(token: str, project_id: str) -> dict[str, Any]:
    if not project_id:
        raise ValueError("FIREBASE_PROJECT_ID is not configured")

    header = jwt.get_unverified_header(token)
    kid = header.get("kid")
    if not kid:
        raise ValueError("Missing key id")

    certs = await _get_google_certs()
    public_key = certs.get(kid)
    if not public_key:
        _cert_cache["fetched_at"] = 0.0
        certs = await _get_google_certs()
        public_key = certs.get(kid)
    if not public_key:
        raise ValueError("Unknown signing key")

    decoded = jwt.decode(
        token,
        public_key,
        algorithms=["RS256"],
        audience=project_id,
        issuer=f"https://securetoken.google.com/{project_id}",
    )
    return decoded
