plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.hastanghubaga"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hastanghubaga"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Hilt Test Runner
        testInstrumentationRunner = "com.example.hastanghubaga.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions { jvmTarget = "11" }

    buildFeatures { compose = true }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    sourceSets {
        getByName("androidTest").manifest.srcFile("src/androidTest/AndroidManifest.xml")
    }
}

dependencies {

    // Kotlin + Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation + Hilt
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")

    debugImplementation("com.google.dagger:hilt-android-testing:2.51")
    kaptDebug("com.google.dagger:hilt-compiler:2.51")

    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.51")

    // Coroutines testing
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Glance Widgets
    implementation("androidx.glance:glance-appwidget:1.0.0")

    // Local Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.3")

    // Android Test (IMPORTANT: Hilt-compatible versions)
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")

    // Compose UI test (must downgrade to match AndroidX Test 1.4)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")

    androidTestImplementation("com.google.truth:truth:1.1.3")

    // Backup
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

}

configurations.all {
    resolutionStrategy {
        force(
            "androidx.test:core:1.4.0",
            "androidx.test:runner:1.4.0",
            "androidx.test:rules:1.4.0",
            "androidx.test.ext:junit:1.1.3",
            "androidx.test.espresso:espresso-core:3.4.0"
        )
    }
}

