plugins {
    // Android + Kotlin
    alias(libs.plugins.android.application)
    kotlin("android")
    alias(libs.plugins.kotlin.serialization)

    // Dependency Injection
    alias(libs.plugins.hilt.android)

    // Annotation Processing
    alias(libs.plugins.ksp)
    kotlin("kapt")
}

android {
    namespace = "com.example.hastanghubaga"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hastanghubaga"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.hastanghubaga.HiltTestRunner"

        // If you really want to disable verifyDatabase in builds:
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.verifyDatabase" to "false"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // You are using the "old" compose compiler wiring. Keep BOM compatible.
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

kotlin {
    jvmToolchain(17)
}

/**
 * You pinned kotlinx.serialization because of Kotlin 1.9.25 compatibility issues in your project.
 * Keep this if you know you need it.
 */
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" &&
            requested.name.startsWith("kotlinx-serialization")
        ) {
            useVersion("1.6.3")
            because("Pinned to 1.6.3 for project compatibility")
        }
    }
}

dependencies {
    /* ----------------------------- */
    /* Core Android / Kotlin         */
    /* ----------------------------- */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.datetime)

    // ✅ only once (you had it twice)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    /* ----------------------------- */
    /* Jetpack Compose (BOM-managed) */
    /* ----------------------------- */
    // ✅ Use a specific BOM to prevent skew across compose artifacts.
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // ✅ Foundation explicit helps ensure clickable/indication code paths align
    implementation("androidx.compose.foundation:foundation")

    // Material3
    implementation("androidx.compose.material3:material3")

    // ✅ Important for your crash: ensure ripple artifact is aligned via BOM
    implementation("androidx.compose.material:material-ripple")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    /* ----------------------------- */
    /* Dependency Injection (Hilt)   */
    /* ----------------------------- */
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Hilt + WorkManager
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    /* ----------------------------- */
    /* WorkManager                   */
    /* ----------------------------- */
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    /* ----------------------------- */
    /* Room (Database)               */
    /* ----------------------------- */
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    /* ----------------------------- */
    /* App Widgets (Glance)          */
    /* ----------------------------- */
    implementation("androidx.glance:glance:1.1.1")
    implementation("androidx.glance:glance-appwidget:1.1.1")

    /* ----------------------------- */
    /* Security                      */
    /* ----------------------------- */
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    /* ----------------------------- */
    /* Testing                       */
    /* ----------------------------- */
    testImplementation(libs.junit)

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.52")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.52")
    kaptTest("com.google.dagger:hilt-compiler:2.52")

    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("com.google.truth:truth:1.4.2")
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}
