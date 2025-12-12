package com.example.hastanghubaga.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hastanghubaga.data.local.converters.TestConverters
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity

/**
 * TestAppDatabase
 *
 * PURPOSE
 * -------
 * A minimal, test-only Room database used exclusively by Android instrumentation
 * and DAO tests. This database intentionally:
 *
 *  • Uses ONLY the entities required by the test under execution
 *  • Does NOT include seeding callbacks
 *  • Does NOT include production-only entities or relations
 *  • Uses TestConverters instead of production converters
 *
 * This isolation prevents flaky tests, long startup times, and
 * Hilt / Room graph conflicts.
 *
 *
 * WHY THIS EXISTS (IMPORTANT)
 * ---------------------------
 * We do NOT reuse AppDatabase for DAO tests because:
 *
 *  • AppDatabase may include seeding callbacks that interfere with tests
 *  • AppDatabase may include entities not needed by the test
 *  • AppDatabase may pull in DAOs that Hilt expects but the test does not provide
 *  • AppDatabase may cause "multiple app roots" or Hilt aggregation failures
 *
 * TestAppDatabase ensures:
 *  ✔ predictable schema
 *  ✔ fast in-memory execution
 *  ✔ zero side effects
 *
 *
 * WHEN TO ADD OR MODIFY THIS FILE
 * -------------------------------
 * Update TestAppDatabase ONLY when:
 *
 *  • You add a new DAO test that needs a new entity
 *  • A test fails due to a missing entity or converter
 *
 * DO NOT:
 *  • Add all production entities "just in case"
 *  • Share this database with production code
 *
 *
 * CHECKLIST — IF TESTS FAIL, VERIFY THESE
 * ---------------------------------------
 *
 * 1️⃣ Entities
 *    - Does this database include ONLY the entities required by the test?
 *    - Missing entities cause Room runtime crashes.
 *
 * 2️⃣ TypeConverters
 *    - Are all enums / custom types used by the entity covered in TestConverters?
 *    - Using production Converters can silently pull in unused dependencies.
 *
 * 3️⃣ Hilt Application
 *    - This database must be used ONLY with TestApp_HiltTestApplication
 *    - Never reference HastangHubagaApp in tests.
 *
 * 4️⃣ Database Module
 *    - TestDatabaseModule must provide THIS database
 *    - Never mix AppDatabase and TestAppDatabase in the same test graph.
 *
 * 5️⃣ In-Memory Usage
 *    - Always use Room.inMemoryDatabaseBuilder(...) for tests
 *    - Never persist data between test runs.
 *
 *
 * COMMON MISTAKES (LEARNED THE HARD WAY 😅)
 * ----------------------------------------
 *
 * ❌ Using AppDatabase in tests
 * ❌ Including unnecessary entities
 * ❌ Forgetting enum converters
 * ❌ Having two @HiltAndroidApp classes in the same compilation unit
 * ❌ Leaving a TestDatabaseModule in both main/ and androidTest/
 *
 *
 * TL;DR
 * -----
 * This file exists to keep DAO tests:
 *
 *  ✔ isolated
 *  ✔ deterministic
 *  ✔ fast
 *  ✔ Hilt-safe
 *
 *  * Test-only Room database used for DAO tests.
 *  *
 *  * This database intentionally avoids:
 *  * - Hilt
 *  * - App callbacks
 *  * - Widgets
 *  * - Production Application initialization
 *  *
 *  * Use this database for:
 *  * - DAO unit tests
 *  * - Flow emission testing
 *  * - SQL correctness validation
 *  *
 *  * DO NOT replace with AppDatabase in tests.
 *  * Doing so may cause:
 *  * - Multiple app root errors
 *  * - Instrumentation crashes
 *  * - Flaky tests
 *
 * If a test fails mysteriously — check this file first.
 */

@Database(
    entities = [
                SupplementUserSettingsEntity::class,
                UserNutritionGoalsEntity::class
               ],

    version = 1,
    exportSchema = false
)
@TypeConverters(TestConverters::class)
abstract class TestAppDatabase : RoomDatabase() {
    abstract fun supplementUserSettingsDao(): SupplementUserSettingsDao
    abstract fun userNutritionGoalsEntityDao(): UserNutritionGoalsEntityDao
}
