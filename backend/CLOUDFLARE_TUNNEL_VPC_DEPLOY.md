# Deploy LearnerLM with Cloudflare Tunnel + Workers VPC

Your **FastAPI `backend/`** stays private on your PC. A **Cloudflare Worker** is the public API. **Tunnel + VPC** connect them.

```
Android app
    ↓ HTTPS
Worker (learnerlm-api.workers.dev)     ← public edge
    ↓ VPC_SERVICE.fetch()
Cloudflare Tunnel (cloudflared)
    ↓
FastAPI on localhost:8080              ← private, never exposed directly
SQLite in backend/data/
```

**Free during VPC beta.** Your PC must run the API + tunnel while users are online.

---

## Overview — 4 parts

| Part | Where | What |
|------|--------|------|
| **1. Backend** | Your PC | `uvicorn` + `.env` secrets |
| **2. Tunnel** | Cloudflare dashboard | `cloudflared` on your PC |
| **3. VPC Service** | Cloudflare dashboard | Registers `localhost:8080` on the tunnel |
| **4. Edge Worker** | `workers-vpc-proxy/` | Proxies traffic to VPC |

---

## Part 1 — Run the private API

```bash
git clone https://github.com/VEXZIONFILE/LearnerLLM.git
cd LearnerLLM/backend
cp .env.example .env
```

Edit `.env`:

```properties
DEBUG=false
OPENROUTER_API_KEY=your-key
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CREDENTIALS_PATH=/full/path/to/firebase-service-account.json
RESEND_API_KEY=re_your_key
REPORT_NOTIFICATION_EMAIL=elijahjmaxwell43@gmail.com
```

```bash
python -m venv .venv
source .venv/bin/activate    # Windows: .venv\Scripts\activate
pip install -r requirements.txt
mkdir -p data
uvicorn learner_api.main:app --host 127.0.0.1 --port 8080
```

Test locally:

```bash
curl http://127.0.0.1:8080/health
```

---

## Part 2 — Cloudflare Tunnel (website)

1. [dash.cloudflare.com](https://dash.cloudflare.com) → **Zero Trust**
2. **Networks** → **Tunnels** → **Create a tunnel**
3. Name: `learnerlm-private` → **Save**
4. Install **cloudflared** on the **same PC** as the API (copy command from dashboard)
5. Wait until status is **Healthy**

**Do not add a public hostname** for the API — VPC reaches it privately through the tunnel.

---

## Part 3 — VPC Service (website)

1. Dashboard → **Workers VPC** (or Zero Trust → search “VPC Services”)
2. **VPC Services** → **Create**
3. Configure:

| Field | Value |
|-------|--------|
| **Service name** | `learnerlm-backend` |
| **Tunnel** | `learnerlm-private` (from Part 2) |
| **Host** | `localhost` |
| **HTTP port** | `8080` |
| **DNS resolver** | Use tunnel as resolver |

4. **Create** → copy the **Service ID** (UUID)

---

## Part 4 — Deploy the edge Worker

### Option A — Cloudflare dashboard (GitHub)

1. **Workers & Pages** → **Import a repository**
2. Repo: `VEXZIONFILE/LearnerLLM`
3. Branch: `LearnerLM` (or your feature branch)
4. **Root directory:** `workers-vpc-proxy`
5. **Deploy command:** `npx wrangler deploy`
6. Before deploy, set in **Variables** or edit `wrangler.jsonc` on GitHub:
   - Replace `REPLACE_WITH_YOUR_VPC_SERVICE_ID` with your Service ID from Part 3

### Option B — From your computer

```bash
cd LearnerLLM/workers-vpc-proxy
npm install
```

Edit `wrangler.jsonc` — paste your Service ID:

```jsonc
"service_id": "e6a0817c-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```

```bash
npx wrangler login
npm run deploy
```

Public URL:

```
https://learnerlm-api.<your-subdomain>.workers.dev
```

Test:

```bash
curl https://learnerlm-api.<your-subdomain>.workers.dev/health
```

---

## Part 5 — Android app

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.<your-subdomain>.workers.dev/
```

Rebuild the app. The app talks to the **Worker URL**, not your PC.

---

## Checklist

- [ ] `uvicorn` running on `127.0.0.1:8080`
- [ ] Tunnel **Healthy**
- [ ] VPC Service → `localhost:8080` on that tunnel
- [ ] `service_id` in `workers-vpc-proxy/wrangler.jsonc`
- [ ] Worker deployed
- [ ] `/health` works on `*.workers.dev` URL
- [ ] `LEARNER_API_BASE_URL` set in Android

---

## Keep running

| Process | Command |
|---------|---------|
| API | `uvicorn learner_api.main:app --host 127.0.0.1 --port 8080` |
| Tunnel | `cloudflared service` (install from dashboard) |

Install both as system services for always-on (see [CLOUDFLARE_TUNNEL_DEPLOY.md](CLOUDFLARE_TUNNEL_DEPLOY.md) systemd example).

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Worker 502 / error | API not running on port 8080 |
| VPC permission error | Account needs **Connectivity Directory Bind** role |
| Wrong backend URL in proxy | `VPC_TARGET_ORIGIN` in `wrangler.jsonc` must be `http://localhost:8080` |
| Tunnel unhealthy | Reinstall `cloudflared` with dashboard token |
| Works locally, not via Worker | `service_id` wrong or tunnel not same as VPC Service |
| `remote: true` in dev | Use `npm run dev` (`wrangler dev --remote`) to hit real VPC |

---

## Tunnel only vs Tunnel + VPC

| | **Tunnel only** | **Tunnel + VPC** (this guide) |
|--|-----------------|-------------------------------|
| Public URL | Your domain → tunnel → API | `*.workers.dev` Worker → VPC → API |
| API on public internet | Yes (via tunnel hostname) | No — only Worker is public |
| Edge caching / Workers features | No | Yes |
| Setup | Simpler | More steps |

---

## Files

| Path | Role |
|------|------|
| `backend/` | Private FastAPI + SQLite |
| `workers-vpc-proxy/` | Public edge proxy Worker |
| `workers/` | Alternative: full API on Cloudflare + D1 (no tunnel) |
