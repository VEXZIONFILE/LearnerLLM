# LearnerLM VPC edge proxy

Thin Cloudflare Worker that forwards public HTTPS traffic to your **private** FastAPI `backend/` via **Workers VPC** + **Cloudflare Tunnel**.

```
Android → Worker (*.workers.dev) → VPC Service → Tunnel → uvicorn on your PC
```

Deploy guide: **[../backend/CLOUDFLARE_TUNNEL_VPC_DEPLOY.md](../backend/CLOUDFLARE_TUNNEL_VPC_DEPLOY.md)**

```bash
npm install
# Set service_id in wrangler.jsonc first
npm run deploy
```
