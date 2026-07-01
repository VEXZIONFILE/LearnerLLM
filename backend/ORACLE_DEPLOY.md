# Deploy LearnerLM API on Oracle Cloud (Always Free)

Oracle Cloud gives you a **free VM** that runs 24/7 — great for this backend: persistent SQLite, long AI requests, no sleep like Render.

**Cost:** $0 on Always Free (if you pick the right shape — see below).

---

## What you get (Always Free)

| Shape | Specs | Good for LearnerLM? |
|-------|--------|---------------------|
| **VM.Standard.A1.Flex** (Ampere ARM) | Up to 4 OCPU, 24 GB RAM | **Best** — plenty of headroom |
| **VM.Standard.E2.1.Micro** (AMD) | 1 OCPU, 1 GB RAM | Works, but tight — use A1 if available |

**Do NOT pick E5.Flex** — that is **paid**, not Always Free.

---

## 1. Create the VM

1. Sign in at https://cloud.oracle.com
2. **Compute** → **Instances** → **Create instance**
3. **Name:** `learnerlm-api`
4. **Image:** Ubuntu 22.04 or 24.04
5. **Shape:** Click **Change shape**
   - **Ampere** → **VM.Standard.A1.Flex**
   - OCPUs: **1** (or 2), Memory: **6 GB** (enough; saves quota)
6. **Networking:** Use default VCN — note the **public IP** after create
7. **SSH keys:** Choose **Generate a key pair for me** → **Download private key**
   - Save the `.key` file (e.g. `ssh-key-2026-07-01.key`)
   - **Use the `.key` file to SSH — NOT the `.pub` file**
8. Click **Create**

Wait until state is **Running**.

---

## 2. Open port 8080 (required)

Oracle blocks incoming traffic by default.

### A — Security list (Oracle console)

1. **Networking** → **Virtual cloud networks** → your VCN
2. **Security Lists** → default security list → **Add Ingress Rules**
3. Add:

| Source CIDR | Protocol | Destination port |
|-------------|----------|------------------|
| `0.0.0.0/0` | TCP | `8080` |

(For production you can restrict source IPs later.)

### B — Ubuntu firewall (on the VM)

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo netfilter-persistent save
```

Or if `ufw` is active:

```bash
sudo ufw allow 8080/tcp
sudo ufw reload
```

---

## 3. SSH into the VM

**Windows (PowerShell or CMD):**

```powershell
ssh -i "C:\Users\YOUR_NAME\Downloads\ssh-key-2026-07-01.key" ubuntu@YOUR_PUBLIC_IP
```

**Mac / Linux:**

```bash
chmod 600 ~/Downloads/ssh-key-2026-07-01.key
ssh -i ~/Downloads/ssh-key-2026-07-01.key ubuntu@YOUR_PUBLIC_IP
```

Replace:
- `ssh-key-2026-07-01.key` → your downloaded **private** key (no `.pub`)
- `YOUR_PUBLIC_IP` → instance public IP from Oracle console

### SSH troubleshooting

| Error | Fix |
|-------|-----|
| `Permission denied (publickey)` | You used the `.pub` file — use the `.key` private key |
| `WARNING: UNPROTECTED PRIVATE KEY` | Run `chmod 600` on the key (Mac/Linux) |
| `Connection timed out` | Check security list has port **22** (SSH) and **8080** (API) |
| IP changed | Oracle free tier may get new IP after stop/start — check console |

---

## 4. Install Docker on the VM

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker ubuntu
```

Log out and back in (or `newgrp docker`):

```bash
docker --version
docker compose version
```

---

## 5. Deploy LearnerLM API

```bash
git clone https://github.com/VEXZIONFILE/LearnerLLM.git
cd LearnerLLM/backend
cp .env.example .env
mkdir -p data secrets
```

Edit `.env`:

```bash
nano .env
```

Set at minimum:

```env
DEBUG=false
FIREBASE_AUTH_DISABLED=false
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_CREDENTIALS_PATH=/app/secrets/firebase.json
OPENROUTER_API_KEY=sk-or-v1-your-key
BILLING_VERIFICATION_DISABLED=true
```

**Firebase JSON** — either:

**Option A — file on disk (simple):**

```bash
nano secrets/firebase.json
# Paste full Firebase service account JSON, save
```

**Option B — env var in docker-compose** (add under `api` → `environment`):

```yaml
FIREBASE_CREDENTIALS_JSON: '{"type":"service_account",...}'
```

Start the API:

```bash
docker compose up -d --build
docker compose ps
curl http://localhost:8080/health
```

Expected: `{"status":"ok","app":"LearnerLM API"}`

From your PC (tests public access):

```bash
curl http://YOUR_PUBLIC_IP:8080/health
```

---

## 6. Android app

In `local.properties`:

```properties
LEARNER_API_BASE_URL=http://YOUR_PUBLIC_IP:8080/
```

Use `http://` and trailing `/`. Rebuild the app.

The project allows HTTP for self-hosted backends (`network_security_config.xml`).

---

## 7. Keep it running (optional)

**Auto-start Docker on reboot:**

```bash
sudo systemctl enable docker
```

Containers already use `restart: unless-stopped` in `docker-compose.yml`.

**Update after code changes:**

```bash
cd ~/LearnerLLM
git pull
cd backend
docker compose up -d --build
```

---

## Oracle vs other hosts

| | **Oracle (this guide)** | **Render free** | **Fly.io** | **Vercel** |
|---|-------------------------|-----------------|------------|------------|
| Cost | $0 Always Free | $0 | Limited free | $0 |
| Always on | Yes | Sleeps 15 min | Can sleep | Cold starts |
| Persistent DB | Yes (`./data`) | Weak on free | Volume | No (`/tmp`) |
| Setup | SSH + Docker | GitHub click | CLI | GitHub click |
| Best for | Long-term free prod | Quick test | Cloud + volume | Prototype only |

---

## Security notes

- Keep `OPENROUTER_API_KEY` only in `.env` on the server
- Use `FIREBASE_AUTH_DISABLED=false` in production
- Port 8080 is open to the internet — Firebase auth protects `/v1/*` routes
- For HTTPS later: add **Caddy** or **nginx** + Let's Encrypt on the VM

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Can't SSH | Private `.key` file, port 22 open in security list |
| `curl` works on VM, not from phone | Open port 8080 in Oracle security list + `ufw` |
| `401` on API | Set Firebase credentials + `FIREBASE_PROJECT_ID` |
| Out of capacity (A1) | Try another availability domain, or use E2.1.Micro |
| Data gone | SQLite lives in `backend/data/` — don't delete that folder |
| Chat offline | Add `OPENROUTER_API_KEY` in `.env`, restart compose |

---

## Quick reference

```bash
# SSH
ssh -i /path/to/your.key ubuntu@PUBLIC_IP

# Logs
cd ~/LearnerLLM/backend && docker compose logs -f

# Restart
docker compose restart

# Stop
docker compose down
```
