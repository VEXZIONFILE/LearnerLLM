# LearnerLM Backend API

FastAPI backend for [LearnerLM](https://github.com/VEXZIONFILE/LearnerLLM). Handles AI tutoring (OpenRouter proxy), Firebase-authenticated users, homework scan quotas, Google Play subscription verification, custom subjects, and learning progress sync.

## Features

| Endpoint | Description |
|----------|-------------|
| `GET /health` | Health check |
| `GET/PATCH /v1/me` | User profile + authoritative subscription tier |
| `POST /v1/chat/messages` | Socratic tutor, study packs, and code help |
| `GET /v1/chat/sessions` | List chat sessions |
| `GET /v1/scans/quota` | Daily homework scan quota |
| `POST /v1/scans` | Record a successful scan |
| `POST /v1/billing/verify` | Verify Google Play purchase |
| `GET/POST/DELETE /v1/subjects` | Custom subjects CRUD |
| `GET /v1/progress` | Study topics + learning streak |

## Quick start (local)

```bash
cd backend
cp .env.example .env
# Add OPENROUTER_API_KEY to .env for live AI responses

python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

mkdir -p data
uvicorn learner_api.main:app --reload --port 8080
```

API docs: http://localhost:8080/docs

### Dev auth (no Firebase)

Set `FIREBASE_AUTH_DISABLED=true` in `.env`, then call APIs with:

```
Authorization: Bearer dev
```

## Docker

```bash
cd backend
cp .env.example .env
docker compose up --build
```

## Android app configuration

In project `local.properties`:

```properties
LEARNER_API_BASE_URL=http://10.0.2.2:8080/
```

(`10.0.2.2` is the Android emulator alias for your machine's localhost.)

## Environment variables

See `.env.example` for all options. Key settings:

- `OPENROUTER_API_KEY` — server-side only; never ship in the APK
- `FIREBASE_CREDENTIALS_PATH` — Firebase Admin service account JSON
- `FIREBASE_AUTH_DISABLED` — `true` for local development
- `BILLING_VERIFICATION_DISABLED` — `true` accepts tokens without Google Play API (dev only)

## Tests

```bash
cd backend
FIREBASE_AUTH_DISABLED=true pytest -q
```

## Security

- All `/v1/*` routes require a valid Firebase ID token (except dev mode).
- OpenRouter API key stays on the server.
- Subscription tier is enforced server-side for AI depth and scan limits.
