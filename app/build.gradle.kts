import java.util.Properties

plugins {
    id("com.android.application")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        load(propsFile.inputStream())
    }
}

fun localProperty(name: String, default: String = ""): String =
    localProperties.getProperty(name, default)
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

android {
    namespace = "com.learner.lm"
    compileSdk = 34

    defaultConfig {
        // Must match package_name in app/google-services.json (Firebase Console).
        applicationId = localProperty("APP_APPLICATION_ID", "com.learnerlm")
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "OPENROUTER_BASE_URL", "\"https://openrouter.ai/api/v1/\"")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${localProperty("OPENROUTER_API_KEY")}\"")
        buildConfigField("String", "APP_REFERER", "\"https://github.com/VEXZIONFILE/LearnerLLM\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.google.mlkit:text-recognition:16.0.1")

    val cameraX = "1.3.4"
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")

    // -------------------------------------------------------------------------
    // Firebase — requires app/google-services.json from Firebase Console
    // https://firebase.google.com/docs/android/setup
    // -------------------------------------------------------------------------

    // Import the Firebase BoM (manages Firebase library versions)
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))

    // Firebase products — do not specify versions when using the BoM
    implementation("com.google.firebase:firebase-analytics")
    // firebase-auth 24.1.0+ requires Kotlin 2.3; pin to 24.0.0 for Kotlin 2.2.x toolchains.
    implementation("com.google.firebase:firebase-auth:24.0.0")

    // Add more Firebase SDKs here if needed:
    // implementation("com.google.firebase:firebase-firestore")
    // https://firebase.google.com/docs/android/setup#available-libraries

    implementation("com.android.billingclient:billing-ktx:7.0.0")

    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
