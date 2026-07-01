# Google Play Subscriptions Setup

Create these subscription products in [Google Play Console](https://play.google.com/console) for app `com.learnerlm`:

| Product ID | Price | Billing period |
|------------|-------|----------------|
| `learnerlm_basic_monthly` | $9.99 | Monthly (Premium) |
| `learnerlm_pro_yearly` | $99.90 | Yearly (Premium — 10 months, 2 months free) |

## Premium pricing

- Premium monthly: **$9.99/month**
- Premium yearly: $9.99 × 10 = **$99.90/year** (2 months free — save **$19.98** vs monthly)

## AI modes (multi-model routing)

| Mode | OpenRouter model | Purpose |
|------|------------------|---------|
| Tutor | `openai/gpt-oss-120b` | Socratic tutoring grades 6–12 |
| Study | `nvidia/nemotron-3-super-120b-a12b` | Summaries, flashcards, quizzes |
| Code Help | `poolside/laguna-m.1` | Debug & explain code (small pieces only) |

**Free users:** standard depth, limited study pack sections, basic code help.  
**Premium users:** deeper explanations, full study packs with quizzes, richer code debugging.

## Firebase Email & Password Auth

1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package `com.learnerlm`
3. Download `google-services.json` → place in `app/google-services.json`
4. In Firebase Console → **Authentication** → **Sign-in method** → enable **Email/Password**

## local.properties example

```properties
sdk.dir=/path/to/Android/Sdk
LEARNER_API_BASE_URL=https://api.yourdomain.com/
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

When filling out the Play Console **AI-generated content** declaration, state that users can report content via the in-chat flag button.

Report submissions are stored in the backend and emailed to **elijahjmaxwell43@gmail.com**. Set `RESEND_API_KEY` as a Cloudflare Worker secret.
