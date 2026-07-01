# Deploy LearnerLM API on Northflank

Northflank is a strong fit for this backend: **Docker from GitHub**, **HTTPS URL**, **persistent volume** for SQLite, and **no SSH**.

**Good for:** long AI chat requests, saved progress/scan data, auto-deploy on git push.

---

## FIX: `stageId "__unstaged"` error

If you see:

```text
spec.stageId "stageId" with value "__unstaged" fails to match the required pattern
```

You are in the **wrong Northflank screen**. Northflank is trying to use a pipeline stage called `__unstaged` (invalid).

### Do this now

1. **Stop** — close Pipeline, Release flow, or Template editor
2. **Delete** any broken pipeline, release flow, or half-created service in the project
3. Go to your **project** → top-right **Create new** → **Service** → **Combined**
4. **Do not** click: Pipeline, Release flow, Template, or “Add to pipeline”
5. If you see a **Stage** field:
   - Clear it completely, **or**
   - Type exactly: `production` (no underscores)
6. Finish with these build settings:
   - Branch: `LearnerLM`
   - Dockerfile: `/Dockerfile`
   - Working directory: `/`
7. Click **Create service**

**Combined service = no pipeline = no `__unstaged` error.**

---

## Important: pick the right branch

The backend is **not on `main`**. Use a branch that includes the `backend/` folder:

| Branch | Has backend? |
|--------|----------------|
| **`LearnerLM`** | Yes |
| **`cursor/learner-lm-android-scaffold-6bf2`** | Yes (latest) |
| `main` | **No** — Android app only |

If Northflank says **“No Dockerfile found”** or **“Could not find working directory”**, you are usually on **`main`**, or the paths are wrong (Northflank needs **leading slashes** — see below).

---

## Northflank path format (read this)

Northflank paths **must start with `/`**. Do **not** use `.` or `backend` alone.

| Wrong | Correct |
|-------|---------|
| `.` | `/` |
| `Dockerfile` | `/Dockerfile` |
| `backend` | `/backend` |
| `backend/Dockerfile` | `/backend/Dockerfile` |

---

## 1. Sign up and connect GitHub

1. Go to https://northflank.com and create an account
2. Create a **team** (or use personal)
3. Create a **project** (e.g. `learnerlm`)
4. **Settings → Git** → connect **GitHub**
5. Allow access to **`VEXZIONFILE/LearnerLLM`**

---

## 2. Create a combined service (use this — not a pipeline)

> **Important:** For LearnerLM, use a **Combined service** only.  
> Do **not** use Pipelines / Release flows — they cause errors like `stageId "__unstaged"` and are meant for separate build + deploy services.

1. In your project: **Create new → Service**
2. Choose **Combined service** (build + deploy in one step)
3. **Name:** `learnerlm-api`
4. **Repository:** `VEXZIONFILE/LearnerLLM`
5. **Branch:** **`LearnerLM`** (not `main`)
6. **Stage:** leave **empty**, or type **`production`** (letters/numbers/hyphens only — no underscores)

### Build settings — copy exactly

**Option A — root Dockerfile (recommended)**

| Field | Value |
|-------|--------|
| **Build type** | Dockerfile |
| **Dockerfile path** | `/Dockerfile` |
| **Build context / working directory** | `/` |

**Option B — backend folder only**

| Field | Value |
|-------|--------|
| **Build type** | Dockerfile |
| **Dockerfile path** | `/backend/Dockerfile` |
| **Build context / working directory** | `/backend` |

Northflank should detect **port 8080** from the Dockerfile.

### Networking

| Field | Value |
|-------|--------|
| **Public port** | `8080` |
| **HTTPS** | Enabled (Northflank provides a `*.code.run` URL) |

### Health check (recommended)

On the service → **Health checks** → add:

| Field | Value |
|-------|--------|
| **Protocol** | HTTP |
| **Path** | `/health` |
| **Port** | `8080` |

---

## 3. Add a persistent volume (important)

Without this, SQLite data can be lost on restart.

1. Open your service → **Volumes** → **Add volume**
2. **Name:** `learnerlm-data`
3. **Size:** 1 GB is enough to start
4. **Container mount path:** `/app/data`

This matches `DATABASE_URL=sqlite+aiosqlite:///./data/learnerlm.db` in the container.

> **Note:** A volume limits the service to **1 instance** — fine for this app.

---

## 4. Environment variables

Service → **Environment** (or create a **Secret group** and attach it).

| Key | Value |
|-----|--------|
| `DEBUG` | `false` |
| `DATABASE_URL` | `sqlite+aiosqlite:///./data/learnerlm.db` |
| `CORS_ORIGINS` | `*` |
| `FIREBASE_AUTH_DISABLED` | `false` |
| `FIREBASE_PROJECT_ID` | your Firebase project ID |
| `FIREBASE_CREDENTIALS_JSON` | paste full Firebase service account JSON |
| `OPENROUTER_API_KEY` | your OpenRouter key |
| `BILLING_VERIFICATION_DISABLED` | `true` |
| `GOOGLE_PLAY_PACKAGE_NAME` | `com.learnerlm` |
| `FREE_DAILY_SCAN_LIMIT` | `3` |

### Firebase JSON

1. Firebase Console → Project settings → **Service accounts**
2. **Generate new private key**
3. Copy the entire JSON into `FIREBASE_CREDENTIALS_JSON`

`entrypoint.sh` writes this to `/app/secrets/firebase.json` at startup.

### Testing without Firebase

| Key | Value |
|-----|--------|
| `FIREBASE_AUTH_DISABLED` | `true` |

---

## 5. Deploy

Click **Create service**. First build takes **5–10 minutes**.

When status is **Running**, open your public URL:

```
https://YOUR-SERVICE--YOUR-PROJECT.code.run/health
```

Expected:

```json
{"status":"ok","app":"LearnerLM API"}
```

API docs: `https://YOUR-SERVICE--YOUR-PROJECT.code.run/docs`

---

## 6. Android app

In `local.properties`:

```properties
LEARNER_API_BASE_URL=https://YOUR-SERVICE--YOUR-PROJECT.code.run/
```

Use `https://` and a trailing `/`. Rebuild the app.

---

## 7. Auto-deploy

Combined services rebuild on push to the linked branch by default.

Push to GitHub → Northflank builds and redeploys automatically.

---

## Why Northflank fits this backend

| Need | Northflank |
|------|------------|
| FastAPI + Docker | Native Dockerfile deploy |
| Long AI requests | Full container, not serverless |
| SQLite persistence | Volume at `/app/data` |
| HTTPS for Android | Built-in |
| Secrets | Env vars / secret groups |
| No SSH | GitHub → deploy |

---

## Northflank vs others

| | **Northflank** | **Render free** | **Vercel** | **Oracle VM** |
|---|----------------|-----------------|------------|---------------|
| SSH needed | No | No | No | Yes |
| Persistent data | Volume | Weak on free | No | Yes |
| HTTPS | Yes | Yes | Yes | Manual |
| Setup | Medium | Easy | Easiest | Hard |

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| **`stageId "__unstaged"` error** | You are in a **Pipeline** or **Release flow**. Delete that setup. Create a **Combined service** instead (no pipeline). If Stage is required, use `production` — not `__unstaged` |
| **Could not find working directory** | Build context must be **`/`** (root) or **`/backend`** — not `.` or `backend` |
| **No Dockerfile found** | Branch = **`LearnerLM`**. Dockerfile path = **`/Dockerfile`** with context **`/`** |
| Build failed | Check **Logs**; confirm branch has `backend/` folder |
| `/health` fails | Check logs; confirm port **8080** |
| `401` on API | Set `FIREBASE_CREDENTIALS_JSON` + `FIREBASE_PROJECT_ID` |
| Data lost after restart | Add volume mounted at `/app/data` |
| Chat offline | Add `OPENROUTER_API_KEY` |
| Permission error on volume | Volume should mount to `/app/data` only (not `/app`) |

---

## View logs

Service → **Logs** — useful for Firebase or OpenRouter errors on startup.

---

## Optional: custom domain

Northflank → your service → **Domains** → add your domain and follow DNS instructions.

---

## Start fresh (if errors keep happening)

1. **Delete** the broken service / pipeline / release flow
2. **Create new → Service → Combined service** (not Pipeline, not Build + Deploy separately)
3. Use settings from this guide:
   - Branch: `LearnerLM`
   - Dockerfile: `/Dockerfile`
   - Working directory: `/`
   - Stage: `production` or leave blank
   - Port: `8080`
   - Volume: `/app/data`
4. Add env vars → **Create**
