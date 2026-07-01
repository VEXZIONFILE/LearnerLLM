# Deploy LearnerLM API on Cloudflare Workers

No Docker. No SSH. Free tier available. Global HTTPS URL for your Android app.

---

## What you need

1. [Cloudflare account](https://dash.cloudflare.com/sign-up) (free)
2. [Node.js 18+](https://nodejs.org/)
3. [uv](https://docs.astral.sh/uv/) (Python package manager)

---

## 1. Install tools

```bash
npm install -g wrangler
uv tool install workers-py
```

Log in:

```bash
npx wrangler login
```

---

## 2. Create D1 database

```bash
cd workers
npx wrangler d1 create learnerlm
```

Copy the `database_id` from the output into `wrangler.jsonc`:

```jsonc
"d1_databases": [
  {
    "binding": "DB",
    "database_name": "learnerlm",
    "database_id": "PASTE_YOUR_ID_HERE"
  }
]
```

Apply schema:

```bash
npx wrangler d1 migrations apply learnerlm --remote
```

For local testing:

```bash
npx wrangler d1 migrations apply learnerlm --local
```

---

## 3. Set secrets

```bash
npx wrangler secret put OPENROUTER_API_KEY
npx wrangler secret put FIREBASE_PROJECT_ID
npx wrangler secret put RESEND_API_KEY
```

Report emails go to **elijahjmaxwell43@gmail.com** by default. Optional overrides in `wrangler.jsonc` vars:

```jsonc
"REPORT_NOTIFICATION_EMAIL": "elijahjmaxwell43@gmail.com",
"REPORT_EMAIL_FROM": "LearnerLM Reports <onboarding@resend.dev>"
```

Sign up at [resend.com](https://resend.com) (free tier), create an API key, and set `RESEND_API_KEY`. Verify your domain in Resend for production `from` addresses.

Optional — dev without Firebase (edit `wrangler.jsonc` vars instead):

```jsonc
"FIREBASE_AUTH_DISABLED": "true"
```

---

## 4. Deploy

```bash
cd workers
npm install
uv run pywrangler deploy
```

Your API will be at:

```
https://learnerlm-api.<your-subdomain>.workers.dev
```

Test:

```bash
curl https://learnerlm-api.<your-subdomain>.workers.dev/health
```

Expected:

```json
{"status":"ok","app":"LearnerLM API"}
```

---

## 5. Android app

In `local.properties`:

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.<your-subdomain>.workers.dev/
```

Rebuild the app.

---

## Local development

```bash
cd workers
npx wrangler d1 migrations apply learnerlm --local
uv run pywrangler dev
```

Open `http://localhost:8787/health`

With dev auth:

```bash
curl -H "Authorization: Bearer dev" http://localhost:8787/v1/me
```

---

## Environment variables

Set in `wrangler.jsonc` `[vars]` or as secrets:

| Key | Secret? | Description |
|-----|---------|-------------|
| `OPENROUTER_API_KEY` | Yes | OpenRouter API key |
| `FIREBASE_PROJECT_ID` | Yes | Firebase project ID |
| `FIREBASE_AUTH_DISABLED` | No | `true` for dev (`Bearer dev`) |
| `BILLING_VERIFICATION_DISABLED` | No | `true` (default) |
| `FREE_DAILY_SCAN_LIMIT` | No | Default `3` |

---

## Limits to know

| Topic | Detail |
|-------|--------|
| **AI chat timeout** | Workers have CPU/time limits; long OpenRouter calls may need paid Workers plan |
| **Python beta** | Uses Cloudflare Python Workers (`python_workers` flag) |
| **D1** | Free tier: 5M reads/day, 100k writes/day — plenty for a school app |
| **Cold starts** | Fast on Workers; much quicker than Render free tier sleep |

---

## Redeploy after code changes

```bash
cd workers
uv run pywrangler deploy
```

Or connect GitHub to Cloudflare Workers for auto-deploy (optional).

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Deploy fails on packages | Run `uv sync` in `workers/`; ensure `python_workers` flag is set |
| `401` on API | Set `FIREBASE_PROJECT_ID` secret; user must be signed in on app |
| Database errors | Run `wrangler d1 migrations apply learnerlm --remote` |
| Chat offline | Add `OPENROUTER_API_KEY` secret |
| CPU time exceeded | FastAPI cold start — retry; consider paid Workers plan for AI |

---

## Why Workers instead of Docker?

| Workers | Docker VPS |
|---------|------------|
| No server to manage | SSH + updates |
| Free HTTPS URL | Configure TLS yourself |
| Global edge | Single region |
| `wrangler deploy` | `docker compose up` |

Perfect if you want **simple hosting** without Northflank/Oracle/SSH pain.
