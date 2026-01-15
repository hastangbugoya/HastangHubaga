package com.example.hastanghubaga

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instrumentation test runner used for Hilt-based Android tests.
 *
 * ## Why this runner is required
 * Hilt cannot use the app's real `@HiltAndroidApp` application class
 * (`HastangHubagaApp`) when running instrumented tests.
 *
 * Instead, Hilt requires a special test-only Application
 * (`HiltTestApplication`) that:
 * - allows test modules to replace production modules
 * - prevents production singletons from leaking into tests
 * - ensures dependency graphs are isolated and deterministic
 *
 * Without this runner, tests will fail with:
 * `IllegalStateException: Hilt test cannot use a @HiltAndroidApp application`
 *
 * ## What this runner does
 * This runner overrides the Application class used at test runtime,
 * forcing Android to launch `HiltTestApplication` instead of the
 * production application.
 *
 * This enables:
 * - `@HiltAndroidTest`
 * - `@TestInstallIn`
 * - in-memory databases
 * - fake repositories
 *
 * ## When this runner is used
 * - Only for instrumented tests (`src/androidTest`)
 * - Never included in production builds
 *
 * ## Important
 * You must reference this runner in `defaultConfig.testInstrumentationRunner`
 * for Hilt instrumented tests to work correctly.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context
    ): Application {
        return super.newApplication(
            cl,
            HiltTestApplication::class.java.name,
            context
        )
    }
}
