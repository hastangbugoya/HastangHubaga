package com.example.hastanghubaga

import android.app.Application
import dagger.hilt.android.testing.CustomTestApplication
/**
 * app/src/androidTest/java/com/example/hastanghubaga/TestApp_HiltTestApplication.kt
 *
 * Marker Application class for Hilt Android tests.
 *
 * PURPOSE
 * -------
 * Declares a test-only Application that Hilt can wrap and generate
 * a proper dependency graph for instrumentation tests.
 *
 * This class should contain:
 * - NO logic
 * - NO overrides
 * - NO initialization code
 *
 * CHECKLIST (MUST VERIFY)
 * -----------------------
 * ✓ Annotated with @HiltAndroidApp
 * ✓ Empty class body
 * ✓ Exists only in androidTest
 *
 * COMMON MISTAKES
 * ---------------
 * ✗ Adding logic here
 * ✗ Placing this in main instead of androidTest
 * ✗ Instantiating this class manually
 *
 * TIPS
 * ----
 * • Hilt generates TestApp_HiltTestApplication_Application
 * • This file only exists to trigger code generation
 */
@CustomTestApplication(Application::class)
interface TestApp_HiltTestApplication