# Self-host LearnerLM on your own server (no public port)

Host the backend on your dad's computer or a home server **without** opening port 8080 on the internet router. No Fly.io, no pay-as-you-go cloud.

## Best approach: **Tailscale** (private VPN)

Your dad's server and your phone join the same private network. Traffic never hits the public internet and **no router port forwarding** is needed.

```
┌─────────────┐     Tailscale (encrypted)      ┌──────────────────┐
│  Your phone │ ◄──────────────────────────► │ Dad's home server │
│  (Android)  │      100.x.x.x only          │  Docker + API     │
└─────────────┘                              └──────────────────┘
        ▲                                              ▲
        │                                              │
   No public URL                                 No open WAN port
```

### On dad's server (Linux, Mac, or Windows with Docker)

**1. Install Docker**  
https://docs.docker.com/get-docker/

**2. Clone the project and configure**

```bash
git clone https://github.com/VEXZIONFILE/LearnerLLM.git
cd LearnerLLM/backend
cp .env.example .env
```

Edit `.env`:

```env
DEBUG=false
FIREBASE_AUTH_DISABLED=false
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_CREDENTIALS_PATH=/app/secrets/firebase.json
OPENROUTER_API_KEY=sk-or-v1-your-key
BILLING_VERIFICATION_DISABLED=true
```

Put Firebase service account JSON at `backend/secrets/firebase.json` (create the folder).

**3. Start the API**

```bash
mkdir -p data secrets
docker compose up -d --build
```

**4. Install Tailscale on the server**

https://tailscale.com/download

```bash
# Linux example
curl -fsSL https://tailscale.com/install.sh | sh
sudo tailscale up
```

Note the server's Tailscale IP (looks like `100.x.x.x`):

```bash
tailscale ip -4
```

**5. Do NOT forward port 8080 on the home router.**  
The API is only reachable on the home LAN and over Tailscale.

### On your phone

1. Install **Tailscale** from the Play Store  
2. Sign in to the **same Tailscale account** (or accept dad's invite to his tailnet)  
3. Turn Tailscale **on**

### Android app config

In `local.properties`:

```properties
LEARNER_API_BASE_URL=http://100.x.x.x:8080/
```

Use dad's **Tailscale IP** from step 4 (not the public internet IP).

Rebuild the app. Chat and scans work anywhere you have internet, but traffic stays on the private Tailscale network.

---

## Option 2: Home Wi‑Fi only (simplest, zero extra software)

If you only use the app **at home on the same Wi‑Fi**:

1. Dad runs `docker compose up -d` on his PC  
2. Find the PC's LAN IP: `192.168.x.x` (Mac: System Settings → Network; Windows: `ipconfig`)  
3. In `local.properties`:

```properties
LEARNER_API_BASE_URL=http://192.168.x.x:8080/
```

**No port forwarding.** Works only on home Wi‑Fi.

---

## Option 3: Bind API to localhost + Tailscale Serve (extra lock-down)

If dad wants the API to listen **only** on the machine itself:

Update `docker-compose.yml` ports:

```yaml
ports:
  - "127.0.0.1:8080:8080"
```

Then expose it only via Tailscale:

```bash
tailscale serve --bg --http 8080 http://127.0.0.1:8080
```

Use the Tailscale machine name in the app:

```properties
LEARNER_API_BASE_URL=http://dads-server.tail-xxxxx.ts.net:8080/
```

---

## Security checklist (dad's server)

| Do | Don't |
|----|--------|
| Keep `OPENROUTER_API_KEY` in `.env` on the server only | Put API keys in the Android app |
| Use Firebase auth (`FIREBASE_AUTH_DISABLED=false`) | Expose the API without auth in production |
| Skip router port forwarding for 8080 | Forward 8080 to the internet unless you know what you're doing |
| Keep Docker + OS updated | Commit `.env` or `secrets/` to git |
| Use Tailscale for remote access | Rely on "security through obscurity" on a public IP |

---

## Costs

| Item | Cost |
|------|------|
| Dad's existing PC / old laptop | $0 |
| Electricity | A few dollars/month |
| Tailscale (personal use) | **Free** for up to 100 devices |
| OpenRouter (AI usage) | Pay per token when students chat — only ongoing cost |

No Fly.io, no monthly VPS bill.

---

## Verify it works

**On dad's server:**

```bash
curl http://localhost:8080/health
```

**On your phone (Tailscale on, same Wi‑Fi or mobile data):**

Open a browser and visit `http://100.x.x.x:8080/health` — you should see `{"status":"ok",...}`.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| App can't connect | Tailscale on? Correct `100.x.x.x` in `local.properties`? Rebuild app |
| `Connection refused` | `docker compose ps` — is the API running? |
| `401 Unauthorized` | Firebase configured? User signed in on the app? |
| Works on Wi‑Fi but not away from home | Use Tailscale (Option 1), not LAN IP |
| Android blocks HTTP | Project includes cleartext allowance for private IPs (see `network_security_config.xml`) |

---

## Google Play subscriptions

Billing still goes through Google Play on the phone. Dad's server can set `BILLING_VERIFICATION_DISABLED=true` in `.env` for family use, or add Google Play API credentials later for strict verification.
