# Security Policy

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

If you discover a security issue in Learner LM, please open a private security advisory on GitHub or contact the maintainers directly.

## Scope

Learner LM handles student learning data. The Android app authenticates with Firebase; all AI requests, scan quotas, and subscription tiers are enforced by the **LearnerLM backend API**.

## Secrets

| Secret | Where it belongs |
|--------|------------------|
| `OPENROUTER_API_KEY` | Backend `.env` only — **never** in the Android app |
| Firebase service account | Backend `.env` (`FIREBASE_CREDENTIALS_PATH`) |
| Google Play credentials | Backend `.env` for purchase verification |
| `LEARNER_API_BASE_URL` | Android `local.properties` (not a secret; points to your API) |

## Best Practices

- Do not hardcode API keys in source code
- Run the backend on a trusted host (Docker, VPS, or cloud)
- Use Firebase ID tokens for all `/v1/*` API calls
- Review OpenRouter and Firebase privacy policies before deploying to students
