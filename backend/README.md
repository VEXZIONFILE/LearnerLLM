# LearnerLM Backend API

FastAPI reference implementation for [LearnerLM](https://github.com/VEXZIONFILE/LearnerLLM). Use this folder for **local development and tests**.

**Production hosting:** deploy **[`workers/`](../workers/CLOUDFLARE_DEPLOY.md)** on Cloudflare Workers (D1 + `wrangler deploy`).

## Features

| Endpoint | Description |
|----------|-------------|
| `GET /health` | Health check |
| `GET/PATCH /v1/me` | User profile + authoritative subscription tier |
| `POST /v1/chat/messages` | Socratic tutor, study packs, and code help |
| `GET /v1/scans/quota` | Daily homework scan quota |
| `POST /v1/scans` | Record a successful scan |
| `POST /v1/billing/verify` | Verify Google Play purchase |
| `GET/POST/DELETE /v1/subjects` | Custom subjects CRUD |
| `GET /v1/progress` | Study topics + learning streak |
| `POST /v1/reports` | Flag/report AI content |

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

## Deploy to production (Cloudflare)

See **[../workers/CLOUDFLARE_DEPLOY.md](../workers/CLOUDFLARE_DEPLOY.md)**.

1. Create D1 database and apply migrations
2. Set secrets (`OPENROUTER_API_KEY`, `FIREBASE_PROJECT_ID`, `RESEND_API_KEY`)
3. `uv run pywrangler deploy`
4. Put your Workers URL in Android `local.properties` as `LEARNER_API_BASE_URL`

## Android app configuration

**Local emulator:**

```properties
LEARNER_API_BASE_URL=http://10.0.2.2:8080/
```

**Production (Cloudflare Workers):**

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.<your-subdomain>.workers.dev/
```

## Environment variables

See `.env.example` for all options. Key settings:

- `OPENROUTER_API_KEY` — server-side only; never ship in the APK
- `FIREBASE_CREDENTIALS_PATH` — Firebase Admin service account JSON (local FastAPI)
- `FIREBASE_AUTH_DISABLED` — `true` for local development
- `RESEND_API_KEY` — report emails via [Resend](https://resend.com)
- `REPORT_NOTIFICATION_EMAIL` — default `elijahjmaxwell43@gmail.com`

## Tests

```bash
cd backend
FIREBASE_AUTH_DISABLED=true pytest -q
```

## Security

- All `/v1/*` routes require a valid Firebase ID token (except dev mode).
- OpenRouter API key stays on the server.
- Subscription tier is enforced server-side for AI depth and scan limits.
