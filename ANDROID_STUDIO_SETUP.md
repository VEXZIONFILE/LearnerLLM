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

## 4. Add your API key (optional)

Copy `local.properties.example` to `local.properties` in the project root:

**Windows:**
```properties
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
OPENROUTER_API_KEY=sk-or-v1-your-key-here
```

**Mac:**
```properties
sdk.dir=/Users/YOUR_NAME/Library/Android/sdk
OPENROUTER_API_KEY=sk-or-v1-your-key-here
```

Android Studio usually creates `sdk.dir` automatically. You only need to add `OPENROUTER_API_KEY`.

Get a key at: https://openrouter.ai/settings/keys

## 5. Run on your phone

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
