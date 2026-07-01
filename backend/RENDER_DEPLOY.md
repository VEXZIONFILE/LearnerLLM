# Deploy LearnerLM API on Render

Import GitHub → add env vars → get a public URL. No tunnel, no PC running 24/7.

**Free tier:** service sleeps after ~15 min idle; first request may take 30–60 seconds.

---

## Step 1 — Sign up

1. [render.com](https://render.com) → **Get Started**
2. Sign in with **GitHub**
3. Allow access to **`VEXZIONFILE/LearnerLLM`**

---

## Step 2 — Create web service

### Option A — Blueprint (easiest)

1. **Dashboard** → **New +** → **Blueprint**
2. Select repo **LearnerLLM**, branch **`LearnerLM`**
3. Render reads `render.yaml` → **Apply**

### Option B — Manual

1. **New +** → **Web Service**
2. Connect **LearnerLLM**

| Field | Value |
|-------|--------|
| **Name** | `learnerlm-api` |
| **Region** | Pick closest to you |
| **Branch** | `LearnerLM` |
| **Root Directory** | `backend` |
| **Runtime** | **Docker** |
| **Instance Type** | **Free** |
| **Health Check Path** | `/health` |

---

## Step 3 — Environment variables

Service → **Environment** → add:

| Key | Value |
|-----|--------|
| `DEBUG` | `false` |
| `DATABASE_URL` | `sqlite+aiosqlite:///./data/learnerlm.db` |
| `CORS_ORIGINS` | `*` |
| `OPENROUTER_API_KEY` | Your OpenRouter key |
| `FIREBASE_PROJECT_ID` | Firebase project ID |
| `FIREBASE_CREDENTIALS_JSON` | Paste **entire** Firebase service account JSON (one line) |
| `BILLING_VERIFICATION_DISABLED` | `true` |
| `RESEND_API_KEY` | Your Resend key |
| `REPORT_NOTIFICATION_EMAIL` | `elijahjmaxwell43@gmail.com` |
| `REPORT_EMAIL_FROM` | `LearnerLM Reports <onboarding@resend.dev>` |
| `FREE_DAILY_SCAN_LIMIT` | `3` |

**Firebase JSON:** Firebase Console → Project settings → Service accounts → **Generate new private key** → copy all of it into `FIREBASE_CREDENTIALS_JSON`.

**Dev only** (skip real Firebase):

| Key | Value |
|-----|--------|
| `FIREBASE_AUTH_DISABLED` | `true` |

---

## Step 4 — Deploy

Click **Create Web Service** or **Deploy**.

First build takes **5–10 minutes**. When status is **Live**, open:

```
https://learnerlm-api.onrender.com/health
```

(Use your actual service name if different.)

Expected:

```json
{"status":"ok","app":"LearnerLM API"}
```

---

## Step 5 — Android app

In `local.properties`:

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.onrender.com/
```

Use your real Render URL with `https://` and trailing `/`. Rebuild the app.

---

## Optional — persistent disk (paid)

Free Render **resets SQLite on redeploy**. For production data that survives redeploys:

1. Upgrade plan or add a **disk** (Render paid features)
2. Mount at `/app/data`
3. Keep `DATABASE_URL=sqlite+aiosqlite:///./data/learnerlm.db`

For testing and early launch, free tier is fine.

---

## Auto-deploy

Every push to **`LearnerLM`** redeploys automatically (if enabled in service settings).

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Build fails | Root directory must be **`backend`** |
| 502 on wake | Free tier waking up — wait 60s, retry |
| `401` on app | Check `FIREBASE_PROJECT_ID` + `FIREBASE_CREDENTIALS_JSON` |
| AI offline | Add `OPENROUTER_API_KEY` |
| Data lost after deploy | Expected on free tier — add disk or use paid plan |

---

## Your public URL

After deploy, copy from Render dashboard:

```
https://<your-service-name>.onrender.com
```

That’s what goes in `LEARNER_API_BASE_URL`.
