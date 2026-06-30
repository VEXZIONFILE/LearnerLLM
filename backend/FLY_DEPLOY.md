# Deploy LearnerLM API on Fly.io

## 1. Install Fly CLI

**Mac:**
```bash
brew install flyctl
```

**Windows (PowerShell):**
```powershell
powershell -Command "iwr https://fly.io/install.ps1 -useb | iex"
```

**Linux:**
```bash
curl -L https://fly.io/install.sh | sh
```

Log in:
```bash
fly auth login
```

## 2. Create the app (first time only)

```bash
cd backend
fly launch --no-deploy
```

When prompted:
- **App name:** `learnerlm-api` (or any unique name — update `fly.toml` if you change it)
- **Region:** pick one close to your users (e.g. `iad`, `lax`, `ams`)
- **Use existing Dockerfile?** → **Yes**
- **PostgreSQL?** → **No**
- **Redis?** → **No**

## 3. Create persistent storage (required)

SQLite lives on a Fly volume. Use the **same region** as `primary_region` in `fly.toml`.

```bash
fly volumes create learnerlm_data --region iad --size 1
```

## 4. Set secrets

```bash
fly secrets set \
  OPENROUTER_API_KEY="sk-or-v1-your-key" \
  FIREBASE_PROJECT_ID="your-firebase-project-id" \
  FIREBASE_CREDENTIALS_JSON="$(cat /path/to/firebase-service-account.json)"
```

Optional — Google Play purchase verification:
```bash
fly secrets set \
  GOOGLE_PLAY_CREDENTIALS_JSON="$(cat /path/to/google-play-service-account.json)" \
  BILLING_VERIFICATION_DISABLED="false"
```

**Dev / testing only** (skip Firebase):
```bash
fly secrets set FIREBASE_AUTH_DISABLED="true"
```

## 5. Deploy

```bash
fly deploy
```

## 6. Verify

```bash
fly open /health
# Expected: {"status":"ok","app":"LearnerLM API"}

fly logs
fly status
```

Your API URL: `https://learnerlm-api.fly.dev` (or your app name).

## 7. Point the Android app

In project `local.properties`:

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.fly.dev/
```

Rebuild the app.

---

## Useful commands

| Command | Purpose |
|---------|---------|
| `fly logs` | Live logs |
| `fly ssh console` | Shell into the machine |
| `fly scale count 1` | Keep one machine always on (fewer cold starts) |
| `fly secrets list` | List configured secrets |
| `fly deploy` | Redeploy after code changes |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| **Volume not found** | Run `fly volumes create learnerlm_data --region <region>` matching `fly.toml` |
| **401 on API calls** | Set `FIREBASE_CREDENTIALS_JSON` and `FIREBASE_PROJECT_ID`; ensure `FIREBASE_AUTH_DISABLED=false` |
| **Chat returns offline fallback** | Set `OPENROUTER_API_KEY` secret and redeploy |
| **Cold start delay** | `fly scale count 1` |
| **Health check failing** | `fly logs` — app may still be starting; check `DATABASE_URL` and volume mount |

## Cost notes

- Fly offers a free allowance; small APIs are often low cost.
- 1 GB volume ≈ a few cents/month.
- `min_machines_running = 0` stops the machine when idle (saves money, adds cold starts).
