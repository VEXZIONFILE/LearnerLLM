# Google Play Subscriptions Setup

Create these subscription products in [Google Play Console](https://play.google.com/console) for app `com.learnerlm`:

| Product ID | Price | Plan |
|------------|-------|------|
| `learnerlm_basic_monthly` | $9.99 | Pro (monthly) |
| `learnerlm_pro_monthly` | $19.99 | Premium (monthly) |
| `learnerlm_pro_yearly` | $99.99 | Mega (yearly) |

## Plan pricing

- **Pro** monthly: **$9.99/month**
- **Premium** monthly: **$19.99/month**
- **Mega** yearly: **$99.99/year**

## AI modes (multi-model routing)

| Mode | OpenRouter model | Purpose |
|------|------------------|---------|
| Tutor | `openai/gpt-oss-120b` | Socratic tutoring grades 6–12 |
| Study | `nvidia/nemotron-3-super-120b-a12b` | Summaries, flashcards, quizzes |
| Code Help | `poolside/laguna-m.1` | Debug & explain code (small pieces only) |
| **Free** | *(pick one below)* | OpenRouter **:free** models — no extra cost |

### Free mode models (`:free` on OpenRouter)

| Variant | OpenRouter model | Purpose |
|---------|------------------|---------|
| Free Tutor | `openai/gpt-oss-120b:free` | Socratic tutoring |
| Free Study | `nvidia/nemotron-3-super-120b-a12b:free` | Study packs |
| Free Code | `poolside/laguna-m.1:free` | Code help |

**Free users:** standard depth, limited study pack sections, basic code help.  
**Pro & Premium users:** deeper explanations, full study packs with quizzes, richer code debugging.

## Firebase Email & Password Auth

1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package `com.learnerlm`
3. Download `google-services.json` → place in `app/google-services.json`
4. In Firebase Console → **Authentication** → **Sign-in method** → enable **Email/Password**

## local.properties example

```properties
sdk.dir=/path/to/Android/Sdk
LEARNER_API_BASE_URL=https://learnerllm-2.onrender.com/
```

## Backend

Deploy **`backend/`** to [Render](https://render.com) — see [backend/RENDER_DEPLOY.md](backend/RENDER_DEPLOY.md). Set `OPENROUTER_API_KEY` and `RESEND_API_KEY` as environment variables. The Android app never receives the OpenRouter key.

After a purchase, the app calls `POST /v1/billing/verify` so subscription tier is enforced server-side.

## Generative AI content reporting (Google Play requirement)

Apps that use generative AI must let users **flag or report offensive AI output inside the app** (no external browser required).

LearnerLM includes:

- A **flag icon** on every AI tutor reply in chat → opens an in-app report dialog
- Reasons: offensive, harmful, inaccurate, spam, or other (optional details)
- Reports are sent to `POST /v1/reports` on your Learner API backend
- **Settings → Safety & AI** explains how reporting works

Report emails are sent to `elijahjmaxwell43@gmail.com` via Resend when `RESEND_API_KEY` is configured on the backend.
