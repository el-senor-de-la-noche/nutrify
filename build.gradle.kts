
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.nutrify"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nutrify"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ML Kit: reconocimiento de texto
    implementation("com.google.mlkit:text-recognition:16.0.0")

// ML Kit: image labeling (clasificar alimentos)
    implementation("com.google.mlkit:image-labeling:17.0.7")

// ML Kit: object detection opcional
    implementation("com.google.mlkit:object-detection:17.0.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- COMPOSE ---
    // BOM de Compose
    implementation(platform(libs.compose.bom))

// UI base
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

// Foundation (si la usas)
    implementation(libs.androidx.foundation)

// ICONOS
    implementation(libs.compose.material.icons.extended)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)

    // NAVIGATION
    implementation(libs.navigation.compose)

    // MLKIT IA REAL
    implementation(libs.mlkit.text.recognition)
    implementation(libs.mlkit.image.labeling)

    // CAMERA X
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Gemini IA

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

