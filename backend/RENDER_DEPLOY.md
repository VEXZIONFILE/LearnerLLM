# Deploy LearnerLM API on Render (free tier)

Render hosts the backend with **no SSH** — connect your GitHub repo and deploy from the dashboard.

**Free tier note:** The service **sleeps after 15 minutes** idle and takes **~30–60 seconds** to wake on the next request. Users may need to **retry once** after opening the app.

---

## 1. Push code to GitHub

Your repo must be on GitHub (e.g. `VEXZIONFILE/LearnerLLM`).

---

## 2. Create a Render account

1. Go to https://render.com and sign up (GitHub login is easiest).
2. Connect your GitHub account.

---

## 3. Create the web service

### Option A — Blueprint (easiest)

1. **Dashboard** → **New +** → **Blueprint**
2. Select your **LearnerLLM** repo
3. Render reads `render.yaml` from the repo root
4. Click **Apply**

### Option B — Manual

1. **New +** → **Web Service**
2. Connect repo **LearnerLLM**
3. Settings:

| Field | Value |
|-------|--------|
| **Name** | `learnerlm-api` |
| **Root Directory** | `backend` |
| **Runtime** | **Docker** |
| **Instance Type** | **Free** |
| **Health Check Path** | `/health` |

---

## 4. Add secret environment variables

In the service → **Environment** → add:

| Key | Value |
|-----|--------|
| `OPENROUTER_API_KEY` | Your OpenRouter key (or leave empty for offline fallback) |
| `FIREBASE_PROJECT_ID` | Your Firebase project ID |
| `FIREBASE_CREDENTIALS_JSON` | Paste the **entire** Firebase service account JSON on one line |

To get Firebase JSON:
1. Firebase Console → Project settings → **Service accounts**
2. **Generate new private key**
3. Open the downloaded `.json` file
4. Copy all of it into `FIREBASE_CREDENTIALS_JSON` in Render

**Dev/testing only** (skip Firebase):

| Key | Value |
|-----|--------|
| `FIREBASE_AUTH_DISABLED` | `true` |

---

## 5. Deploy

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

## 6. Android app

In `local.properties`:

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.onrender.com/
```

Use your real Render URL with `https://` and trailing `/`.

Rebuild and install the app.

---

## 7. Optional — reduce sleep annoyance (free hack)

Use [UptimeRobot](https://uptimerobot.com) or [cron-job.org](https://cron-job.org) to ping every **14 minutes**:

```
GET https://learnerlm-api.onrender.com/health
```

Not guaranteed, but can reduce cold starts.

**Always on:** upgrade to **Starter ($7/month)** in Render → Settings → Instance Type.

---

## Limitations (free tier)

| Topic | Detail |
|-------|--------|
| **Sleep** | 15 min idle → ~1 min wake |
| **Database** | SQLite on ephemeral disk — data may reset on redeploy |
| **750 hours/month** | Shared across all free services in your workspace |
| **AI timeouts** | OK once awake (120s read timeout in app) |

For production with many users, consider **Starter** plan or **Fly.io** / a VM.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Build failed | Check **Logs** tab; ensure **Root Directory** = `backend` |
| `/health` 502 | Wait for deploy to finish; check logs for Python errors |
| App 401 | Set `FIREBASE_CREDENTIALS_JSON` + `FIREBASE_PROJECT_ID` |
| First app request fails | Normal on free tier — **retry** after ~1 min |
| Chat offline messages | Add `OPENROUTER_API_KEY` in Environment |

---

## Redeploy after code changes

Push to GitHub → Render auto-deploys (if enabled) or click **Manual Deploy**.
