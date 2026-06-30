# Google Play Subscriptions Setup

Create these subscription products in [Google Play Console](https://play.google.com/console) for app `com.learner.lm`:

| Product ID | Price | Billing period |
|------------|-------|----------------|
| `learnerlm_basic_monthly` | $9.99 | Monthly |
| `learnerlm_pro_monthly` | $19.99 | Monthly |
| `learnerlm_pro_yearly` | $199.90 | Yearly (10 months of Pro — 2 months free) |

## Yearly savings

- Pro monthly × 12: $19.99 × 12 = **$239.88/year**
- Pro yearly: $19.99 × 10 = **$199.90/year** (2 months free — save **$39.98**)

## Firebase Email & Password Auth

1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package `com.learner.lm`
3. Download `google-services.json` → place in `app/google-services.json`
4. In Firebase Console → **Authentication** → **Sign-in method** → enable **Email/Password**

Users stay signed in automatically after closing the app (Firebase persists the session on device).

## local.properties example

```properties
sdk.dir=/path/to/Android/Sdk
OPENROUTER_API_KEY=sk-or-v1-your-key
```
