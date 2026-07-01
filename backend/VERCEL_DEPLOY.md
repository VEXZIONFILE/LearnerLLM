# Deploy LearnerLM API on Vercel (free Hobby plan)

Vercel can host the FastAPI backend with **no SSH**. Connect GitHub and deploy.

**Important limits on free Vercel:**
- Database (SQLite) is **not persistent** — user/scan data may reset on redeploys/cold starts. OK for testing; use Render/Fly/VM for production data.
- First request after idle can be slow (cold start).
- Hobby plan: **5 minute** max per request (enough for AI chat).

---

## 1. Sign up

1. Go to https://vercel.com
2. Sign in with **GitHub**
3. Allow Vercel to access your repos

---

## 2. Import the project

1. **Add New…** → **Project**
2. Import **`VEXZIONFILE/LearnerLLM`**
3. Configure:

| Setting | Value |
|---------|--------|
| **Root Directory** | `backend` |
| **Framework Preset** | Other (or FastAPI if detected) |

4. **Environment Variables** — add:

| Key | Value |
|-----|--------|
| `DATABASE_URL` | `sqlite+aiosqlite:////tmp/learnerlm.db` |
| `DEBUG` | `false` |
| `FIREBASE_PROJECT_ID` | your Firebase project ID |
| `FIREBASE_CREDENTIALS_JSON` | paste full Firebase service account JSON |
| `OPENROUTER_API_KEY` | optional (empty = offline fallback) |
| `BILLING_VERIFICATION_DISABLED` | `true` |
| `FIREBASE_AUTH_DISABLED` | `false` |

**Testing without Firebase:** set `FIREBASE_AUTH_DISABLED` = `true`

5. Click **Deploy**

First deploy takes **5–10 minutes**.

---

## 3. Test

When deploy succeeds, open:

```
https://YOUR-PROJECT.vercel.app/health
```

Expected:

```json
{"status":"ok","app":"LearnerLM API"}
```

---

## 4. Android app

In `local.properties`:

```properties
LEARNER_API_BASE_URL=https://YOUR-PROJECT.vercel.app/
```

Use `https://` and trailing `/`. Rebuild the app.

---

## Firebase JSON

1. Firebase Console → Project settings → **Service accounts**
2. **Generate new private key**
3. Copy the entire JSON into `FIREBASE_CREDENTIALS_JSON` in Vercel (one blob)

---

## Vercel vs Render

| | **Vercel** | **Render** |
|---|-----------|------------|
| Free HTTPS | ✅ | ✅ |
| SSH needed | ❌ | ❌ |
| AI timeout (free) | 5 min | OK once awake |
| Sleeps when idle | Cold start | 15 min sleep |
| Persistent database | ❌ SQLite ephemeral | ⚠️ limited |
| Easiest for this app | Good | **Slightly better for data** |

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Build failed | Root Directory must be **`backend`** |
| 401 on API | Set `FIREBASE_CREDENTIALS_JSON` + `FIREBASE_PROJECT_ID` |
| 504 timeout | Rare on Hobby; retry |
| Data lost | Expected on Vercel — use Render/VM for persistent DB |
| Chat offline | Add `OPENROUTER_API_KEY` in Vercel env |

---

## Redeploy

Push to GitHub → Vercel auto-deploys.
