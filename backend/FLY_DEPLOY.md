# Deploy LearnerLM API on Fly.io

Fly.io runs the **`backend/`** Docker image with a persistent volume for SQLite.

---

## Is Fly.io free?

| | Detail |
|--|--------|
| **Trial** | Free credits when you sign up |
| **After trial** | Pay for usage — small API ≈ a few $/month |
| **Cost saver** | `auto_stop_machines` is on — app sleeps when idle |

---

## Option A — Deploy from the Fly.io website (no CLI)

### 1. Launch from GitHub

1. [fly.io/dashboard](https://fly.io/dashboard) → sign up (GitHub login)
2. **Launch App** → **Launch from GitHub**
3. Connect GitHub → select **`VEXZIONFILE/LearnerLLM`**

| Setting | Value |
|---------|--------|
| **Branch** | `LearnerLM` |
| **Root directory** | `backend` |

Fly detects `backend/Dockerfile` and `backend/fly.toml`.

### 2. Secrets (dashboard → your app → Secrets)

| Secret | Value |
|--------|--------|
| `OPENROUTER_API_KEY` | OpenRouter key |
| `FIREBASE_PROJECT_ID` | Firebase project ID |
| `FIREBASE_CREDENTIALS_JSON` | Full Firebase service account JSON |
| `RESEND_API_KEY` | Resend `re_...` key |

### 3. Volume (dashboard → Volumes)

| Setting | Value |
|---------|--------|
| **Name** | `learnerlm_data` |
| **Mount path** | `/app/data` |
| **Size** | 1 GB |
| **Region** | Same as app (e.g. `iad`) |

### 4. Deploy & test

Deploy from the dashboard. Then open:

```
https://learnerlm-api.fly.dev/health
```

Expected: `{"status":"ok","app":"LearnerLM API"}`

### 5. Auto-deploy (optional)

App → **Deployments** → **Settings** → enable **Auto Deploy** → branch `LearnerLM`.

### 6. Android app

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.fly.dev/
```

Use your real Fly URL if the app name differs.

---

## Option B — CLI

```bash
# Install: https://fly.io/docs/hands-on/install-flyctl/
fly auth login
cd backend
fly launch --no-deploy
fly volumes create learnerlm_data --region iad --size 1
fly secrets set OPENROUTER_API_KEY=... FIREBASE_PROJECT_ID=... \
  FIREBASE_CREDENTIALS_JSON='...' RESEND_API_KEY=...
fly deploy
```

---

## Files in this folder

| File | Purpose |
|------|---------|
| `Dockerfile` | Production image (FastAPI + uvicorn) |
| `fly.toml` | Fly app config, health check, volume mount |
| `.dockerignore` | Smaller Docker builds |

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| App name taken | Change `app = "..."` in `fly.toml` |
| Volume region mismatch | Volume region must match `primary_region` |
| `401` errors | Set Firebase secrets |
| AI offline | Set `OPENROUTER_API_KEY` |
| Data wiped | Volume `learnerlm_data` → `/app/data` |
| Slow first request | Machine was stopped — wakes automatically |
