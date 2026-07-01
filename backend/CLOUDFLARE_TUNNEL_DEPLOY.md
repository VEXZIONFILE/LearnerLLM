# Deploy LearnerLM API with Cloudflare Tunnel (no Workers, no VPC)

Run **`backend/`** on your computer (or any server). **Cloudflare Tunnel** gives it a public HTTPS URL — no port forwarding, no Fly.io, no Workers.

```
Android app  →  https://learnerlm-api.yourdomain.com  →  Tunnel  →  FastAPI on your PC (localhost:8080)
```

**Free:** Cloudflare Tunnel is free. Your PC must stay on (or use a small VPS) while the API runs.

---

## What you need

- [Cloudflare account](https://dash.cloudflare.com/sign-up) (free)
- A domain on Cloudflare **or** use a free `*.cfargotunnel.com` test hostname
- Python 3.11+ on the machine running the API
- Keys: OpenRouter, Firebase, Resend (same as other deploy guides)

---

## Part 1 — Start the API on your machine

### 1. Clone & configure

```bash
git clone https://github.com/VEXZIONFILE/LearnerLLM.git
cd LearnerLLM/backend
cp .env.example .env
```

Edit `.env` and set at minimum:

```properties
DEBUG=false
OPENROUTER_API_KEY=your-key
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CREDENTIALS_PATH=/full/path/to/firebase-service-account.json
RESEND_API_KEY=re_your_key
REPORT_NOTIFICATION_EMAIL=elijahjmaxwell43@gmail.com
```

Or paste JSON instead of a file path:

```properties
FIREBASE_CREDENTIALS_JSON={"type":"service_account",...}
```

### 2. Install & run

```bash
python -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
mkdir -p data
uvicorn learner_api.main:app --host 127.0.0.1 --port 8080
```

Leave this terminal open. Test locally:

```bash
curl http://127.0.0.1:8080/health
```

Expected: `{"status":"ok","app":"LearnerLM API"}`

---

## Part 2 — Create the tunnel (Cloudflare website)

### 1. Open Tunnels

1. [dash.cloudflare.com](https://dash.cloudflare.com)
2. Select your account
3. Go to **Zero Trust** (or **Networks** → **Connectors** → **Cloudflare Tunnels**)
4. **Networks** → **Tunnels** → **Create a tunnel**

### 2. Name the tunnel

- Name: `learnerlm-api`
- **Save tunnel**

### 3. Install `cloudflared` on the same machine as the API

Pick your OS on the screen and run the command Cloudflare shows. Example (Linux):

```bash
# Cloudflare dashboard gives you the exact command + token
sudo cloudflared service install <YOUR_TUNNEL_TOKEN>
```

Or run in foreground for testing:

```bash
cloudflared tunnel run --token <YOUR_TUNNEL_TOKEN>
```

Wait until the dashboard shows the tunnel as **Healthy**.

### 4. Add a public hostname

Still in the tunnel setup → **Public Hostname** → **Add a public hostname**

| Field | Value |
|-------|--------|
| **Subdomain** | `learnerlm-api` (or any name) |
| **Domain** | Your domain on Cloudflare, or use the suggested hostname |
| **Service type** | HTTP |
| **URL** | `localhost:8080` or `127.0.0.1:8080` |

Save.

Your public URL will look like:

```
https://learnerlm-api.yourdomain.com
```

or a Cloudflare-provided hostname for testing.

### 5. Test the public URL

```bash
curl https://learnerlm-api.yourdomain.com/health
```

Same JSON as local — you're live.

---

## Part 3 — Android app

In `local.properties`:

```properties
LEARNER_API_BASE_URL=https://learnerlm-api.yourdomain.com/
```

Use your real tunnel URL with `https://` and a trailing `/`. Rebuild the app.

---

## Keep it running

| Setup | How |
|-------|-----|
| **Dev / testing** | Two terminals: `uvicorn ...` + `cloudflared tunnel run` |
| **Always on** | Install `cloudflared` as a service (dashboard command) + run uvicorn as a service or use `screen`/`tmux` |
| **Production** | Small VPS or always-on PC; both API and cloudflared start on boot |

### Optional: run API on boot (Linux systemd)

Create `/etc/systemd/system/learnerlm-api.service`:

```ini
[Unit]
Description=LearnerLM API
After=network.target

[Service]
User=YOUR_USER
WorkingDirectory=/path/to/LearnerLLM/backend
EnvironmentFile=/path/to/LearnerLLM/backend/.env
ExecStart=/path/to/LearnerLLM/backend/.venv/bin/uvicorn learner_api.main:app --host 127.0.0.1 --port 8080
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable --now learnerlm-api
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Tunnel not healthy | Re-run `cloudflared` with the token from the dashboard |
| 502 Bad Gateway | API not running — start `uvicorn` on port 8080 |
| 401 from app | Check Firebase credentials in `.env` |
| AI offline | Set `OPENROUTER_API_KEY` in `.env` |
| URL works then stops | PC slept or uvicorn stopped — keep machine awake |
| No custom domain | Add a domain to Cloudflare or use the default tunnel hostname |

---

## Tunnel vs Workers vs VPC

| | **Tunnel only** (this guide) | **Workers + D1** |
|--|------------------------------|------------------|
| Runs on | Your PC / VPS | Cloudflare edge |
| PC must stay on | Yes | No |
| Setup | Dashboard + uvicorn | D1 + deploy `workers/` |
| Best for | Home lab, full Python, no Worker limits | $0, no server to manage |

**You do not need Workers VPC** for tunnel-only — the tunnel already exposes your API publicly.
