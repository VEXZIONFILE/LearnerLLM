# Open in Android Studio (ZIP Download)

Follow these steps to download Learner LM and open it in Android Studio.

## 1. Download the ZIP

**Direct download link:**

https://github.com/VEXZIONFILE/LearnerLLM/archive/refs/heads/cursor/learner-lm-android-scaffold-6bf2.zip

Or on GitHub:
1. Go to https://github.com/VEXZIONFILE/LearnerLLM
2. Make sure branch **`cursor/learner-lm-android-scaffold-6bf2`** is selected
3. Click the green **Code** button → **Download ZIP**

## 2. Extract the ZIP

Extract the ZIP file. You will get a folder like:

```
LearnerLLM-cursor-learner-lm-android-scaffold-6bf2/
```

## 3. Open in Android Studio

1. Open **Android Studio**
2. Click **Open** (not Import)
3. Select the extracted folder (the one that contains `settings.gradle.kts`)
4. Click **OK**
5. If asked, click **Trust Project**
6. Wait for **Gradle Sync** to finish (first time may take 5–10 minutes)

## 4. Start the backend API

The Android app talks to the LearnerLM backend for AI chat, scan quotas, and subscriptions.

```bash
cd backend
cp .env.example .env
# Add OPENROUTER_API_KEY to .env for live AI responses

pip install -r requirements.txt
mkdir -p data
FIREBASE_AUTH_DISABLED=true uvicorn learner_api.main:app --host 0.0.0.0 --port 8080
```

See [backend/README.md](backend/README.md) for production Firebase and billing setup.  
**Production hosting:** [backend/FLY_DEPLOY.md](backend/FLY_DEPLOY.md) (Fly.io).

## 5. Configure the Android app

Copy `local.properties.example` to `local.properties` in the project root:

**Windows:**
```properties
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
LEARNER_API_BASE_URL=http://10.0.2.2:8080/
```

**Mac:**
```properties
sdk.dir=/Users/YOUR_NAME/Library/Android/sdk
LEARNER_API_BASE_URL=http://10.0.2.2:8080/
```

Android Studio usually creates `sdk.dir` automatically. Add `LEARNER_API_BASE_URL` pointing at your backend (`10.0.2.2` is the emulator alias for your computer's localhost).

For a physical device on the same Wi‑Fi, use your computer's LAN IP, e.g. `http://192.168.1.50:8080/`.

## 6. Firebase Auth (required)

1. Open [Firebase Console](https://console.firebase.google.com/) → your project
2. Add an Android app with package name `com.learnerlm` (or your `APP_APPLICATION_ID`)
3. Download `google-services.json` into `app/`
4. Enable **Email/Password** sign-in under Authentication

The backend verifies Firebase ID tokens on every API request.

## 7. Run on your phone

1. Enable **USB debugging** on your phone (Settings → Developer options)
2. Connect phone via USB
3. Select your phone in the device dropdown (top toolbar)
4. Select the **app** run configuration
5. Click the green **Run** button

## Troubleshooting

| Problem | Fix |
|---------|-----|
| **No module** in Run Configuration | Wait for Gradle sync to finish. Use **File → Sync Project with Gradle Files** |
| **SDK location not found** | Create `local.properties` with `sdk.dir` (see step 4) |
| Opened wrong folder | Open the folder that contains `settings.gradle.kts` and `app/` |
| Gradle sync failed | Install **Android SDK API 34** via **Tools → SDK Manager** |
| **Kotlin metadata / kspDebugKotlin failed** | Sync after pulling latest `build.gradle.kts`. Project uses **Kotlin 2.2.21**, **KSP 2.2.21-2.0.5**, and **Room 2.7+** for Firebase BoM 34.x compatibility |
| **Could not load scan quota** | Start the backend API and set `LEARNER_API_BASE_URL` in `local.properties` |
| **No matching client found for package name** | `applicationId` must match `package_name` in `app/google-services.json`. Default is `com.learnerlm`. Re-download `google-services.json` from Firebase if you used a different package, or set `APP_APPLICATION_ID` in `local.properties` to match your file |

## Project structure (what you should see)

```
LearnerLLM-.../
├── app/                    ← Android app module
├── gradle/
├── settings.gradle.kts     ← must be at this level
├── build.gradle.kts
├── gradlew
└── local.properties.example
```
