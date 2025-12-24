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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

kotlin {
    jvmToolchain(17)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx"
            && requested.name.startsWith("kotlinx-serialization")
        ) {
            useVersion("1.6.3")
            because("Kotlin 1.9.25 is incompatible with kotlinx.serialization 1.7.x")
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")


    /* ----------------------------- */
    /* Jetpack Compose (BOM-managed) */
    /* ----------------------------- */
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.testManifest)


    /* ----------------------------- */
    /* Dependency Injection (Hilt)   */
    /* ----------------------------- */
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    // Hilt + Compose navigation
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

    // Room annotation processor (KSP)
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
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
//        arg("room.verifyDatabase", "false")
    }
}

//kapt {
//    correctErrorTypes = true
//}

