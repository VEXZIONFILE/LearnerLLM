# LearnerLM API — Cloudflare Workers

FastAPI backend for LearnerLM, deployed on **Cloudflare Workers** with **D1** (no Docker).

Same REST API as `backend/` — the Android app uses the same endpoints and JSON shapes.

## Stack

| Piece | Technology |
|-------|------------|
| Runtime | Cloudflare Workers (Python) |
| Framework | FastAPI via `asgi.fetch` |
| Database | Cloudflare D1 (SQLite at the edge) |
| Auth | Firebase ID tokens (JWT verify, no `firebase-admin`) |
| AI | OpenRouter proxy |

## Quick deploy

See **[CLOUDFLARE_DEPLOY.md](CLOUDFLARE_DEPLOY.md)** for the full guide.

```bash
cd workers
npm install
uv tool install workers-py

# Create D1 database
npx wrangler d1 create learnerlm
# Copy database_id into wrangler.jsonc

npx wrangler d1 migrations apply learnerlm --remote

# Secrets
npx wrangler secret put OPENROUTER_API_KEY
npx wrangler secret put FIREBASE_PROJECT_ID

# Deploy
uv run pywrangler deploy
```

## Local dev

```bash
cd workers
npx wrangler d1 migrations apply learnerlm --local
uv run pywrangler dev
```

Test: `http://localhost:8787/health`

Dev auth: set `FIREBASE_AUTH_DISABLED=true` in `wrangler.jsonc` vars, use `Authorization: Bearer dev`.

## API endpoints

| Method | Path |
|--------|------|
| GET | `/health` |
| GET/PATCH | `/v1/me` |
| POST | `/v1/chat/messages` |
| GET | `/v1/chat/sessions` |
| GET | `/v1/scans/quota` |
| POST | `/v1/scans` |
| POST | `/v1/billing/verify` |
| GET/POST/DELETE | `/v1/subjects` |
| GET | `/v1/progress` |

## Android app

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.YOUR_SUBDOMAIN.workers.dev/
```

## vs `backend/` (Docker)

| | **Workers** (`workers/`) | **Docker** (`backend/`) |
|---|--------------------------|-------------------------|
| Hosting | Cloudflare Workers | Docker / VM / Render / Fly |
| Database | D1 | SQLite file |
| Deploy | `pywrangler deploy` | `docker compose` |
| Cold starts | Edge, fast | Always-on server |

Use **Workers** for simple global HTTPS with no server management.  
Use **Docker backend** if you need long-running jobs or full Python package ecosystem.
