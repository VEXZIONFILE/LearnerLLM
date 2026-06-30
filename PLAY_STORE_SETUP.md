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

## Firebase + Google Sign-In

1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package `com.learner.lm`
3. Download `google-services.json` → place in `app/google-services.json`
4. Enable **Google** sign-in in Firebase Authentication
5. Copy the **Web client ID** to `local.properties`:

```properties
GOOGLE_WEB_CLIENT_ID=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com
```

## local.properties example

```properties
sdk.dir=/path/to/Android/Sdk
OPENROUTER_API_KEY=sk-or-v1-your-key
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```
