# LearnerLM API — Cloudflare Workers

FastAPI backend for LearnerLM, deployed on **Cloudflare Workers** with **D1**.

Same REST API as `backend/` — the Android app uses the same endpoints and JSON shapes.

## Stack

| Piece | Technology |
|-------|------------|
| Runtime | Cloudflare Workers (Python) |
| Framework | FastAPI via `asgi.fetch` |
| Database | Cloudflare D1 (SQLite at the edge) |
| Auth | Firebase ID tokens (JWT verify) |
| AI | OpenRouter proxy |
| Report emails | Resend |

## Quick deploy

See **[CLOUDFLARE_DEPLOY.md](CLOUDFLARE_DEPLOY.md)** for the full guide.

```bash
cd workers
npm install
uv tool install workers-py

npx wrangler d1 create learnerlm
# Copy database_id into wrangler.jsonc

npx wrangler d1 migrations apply learnerlm --remote

npx wrangler secret put OPENROUTER_API_KEY
npx wrangler secret put FIREBASE_PROJECT_ID
npx wrangler secret put RESEND_API_KEY

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
| GET | `/v1/scans/quota` |
| POST | `/v1/scans` |
| POST | `/v1/billing/verify` |
| GET/POST/DELETE | `/v1/subjects` |
| GET | `/v1/progress` |
| POST | `/v1/reports` |

## Android app

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.YOUR_SUBDOMAIN.workers.dev/
```

## `backend/` folder

Use `backend/` only for **local development and pytest**. Production runs from this `workers/` directory.
