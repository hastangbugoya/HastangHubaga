package com.example.hastanghubaga.domain.usecase.supplement

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.repository.SupplementRepositoryImpl
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Instrumented tests for [GetSupplementsWithUserSettingsForDateUseCase].
 *
 * These tests verify that supplements are correctly scheduled for a given date
 * based on:
 * - The supplement's frequency rules
 * - The configured dose anchor type (e.g., BREAKFAST)
 * - The user's default anchor times stored in the database
 *
 * An in-memory Room database is used to ensure isolation and repeatability.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class GetSupplementsWithUserSettingsForDateUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: GetSupplementsWithUserSettingsForDateUseCase

    /**
     * Sets up an in-memory database and seeds required baseline data.
     *
     * This includes:
     * - A default event time for the BREAKFAST anchor (08:00)
     * - A single DAILY supplement anchored to BREAKFAST
     *
     * The database is reset before each test to ensure deterministic behavior.
     */
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        useCase = GetSupplementsWithUserSettingsForDateUseCase(
            supplementRepository = SupplementRepositoryImpl(
                supplementDao = db.supplementEntityDao(),
                supplementUserSettingsDao = db.supplementUserSettingsDao(),
                ingredientDao = db.ingredientEntityDao(),
                supplementDailyLogDao = db.supplementDailyLogDao(),
                dailyStartTimeDao = db.dailyStartTimeDao(),
                eventTimeDao = db.eventTimeDao()

            ),
            eventTimeDao = db.eventTimeDao()
        )

        runTest {
            // Seed default anchor time (BREAKFAST → 08:00)
            db.eventTimeDao().upsertDefault(
                EventDefaultTimeEntity(
                    anchor = DoseAnchorType.BREAKFAST,
                    timeSeconds = LocalTime.of(8, 0).toSecondOfDay()
                )
            )

            // Insert one supplement
            db.supplementEntityDao().insertSupplement(
                SupplementEntity(
                    id = 1L,
                    name = "Vitamin C",
                    doseAnchorType = DoseAnchorType.BREAKFAST,
                    servingsPerDay = 1.0,
                    frequencyType = FrequencyType.DAILY,
                    brand = "Brand X",
                    notes = "Notes",
                    recommendedServingSize = 2.0,
                    recommendedDoseUnit = SupplementDoseUnit.MG,
                    recommendedWithFood = null,
                    recommendedLiquidInOz = null,
                    recommendedTimeBetweenDailyDosesMinutes = null,
                    avoidCaffeine = null,
                    frequencyInterval = null,
                    weeklyDays = null,
                    offsetMinutes = null,
                    customDose = null,
                    customDoseUnit = null,
                    startDate = null,
                    lastTakenDate = null,
                    isActive = true)
                )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * Verifies that a single DAILY supplement anchored to BREAKFAST
     * is scheduled exactly once at the user's default breakfast time (08:00).
     *
     * Given:
     * - A DAILY supplement with a BREAKFAST anchor
     * - A default BREAKFAST time of 08:00
     *
     * When:
     * - The use case is executed for a given date
     *
     * Then:
     * - Exactly one supplement is returned
     * - The supplement contains exactly one scheduled time
     * - The scheduled time is 08:00
     */
    @Test
    fun happyPath_singleDailyBreakfastSupplement_schedulesAtEightAm() = runTest {
        // ✅ kotlinx.datetime.LocalDate
        DomainTimePolicy.todayLocal(Clock.System)

        val result = useCase(
            DomainTimePolicy.todayLocal(Clock.System)
        ).first()
        assertThat(result).hasSize(1)

        val scheduledTimes = result.first().scheduledTimes

        assertThat(scheduledTimes).hasSize(1)

        // ✅ kotlinx.datetime.LocalTime
        assertThat(scheduledTimes.first()).isEqualTo(kotlinx.datetime.LocalTime(8, 0))
    }
}
