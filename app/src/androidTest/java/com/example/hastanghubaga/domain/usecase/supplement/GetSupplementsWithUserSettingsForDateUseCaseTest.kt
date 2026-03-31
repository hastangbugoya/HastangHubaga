package com.example.hastanghubaga.domain.usecase.supplement

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleFixedTimeEntity
import com.example.hastanghubaga.data.repository.SupplementRepositoryImpl
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.timing.ApplyAnchorOffsetUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveAnchorTimeUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [GetSupplementsWithUserSettingsForDateUseCase].
 *
 * These tests validate the current supplement scheduling pipeline end-to-end:
 * - persisted parent schedule rows
 * - fixed child timing rows
 * - anchored child timing rows
 * - daily / weekly recurrence behavior
 * - enabled / disabled schedule behavior
 * - multiple schedule rows for one supplement
 *
 * Notes:
 * - These tests intentionally focus on the scheduling engine, not UI
 * - Activities are faked as empty for this first batch
 * - Legacy supplement-level fallback is not the main subject here
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class GetSupplementsWithUserSettingsForDateUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: GetSupplementsWithUserSettingsForDateUseCase

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
                ingredientDao = db.ingredientEntityDao(),
                supplementDailyLogDao = db.supplementDailyLogDao(),
                dailyStartTimeDao = db.dailyStartTimeDao(),
                eventTimeDao = db.eventTimeDao(),
                supplementUserSettingsDao = db.supplementUserSettingsDao(),
                supplementNutritionDao = db.supplementNutritionDao(),
                supplementScheduleDao = db.supplementScheduleDao()
            ),
            activityRepository = FakeActivityRepository(),
            eventTimeDao = db.eventTimeDao(),
            resolveAnchorTimeUseCase = ResolveAnchorTimeUseCase(),
            applyAnchorOffsetUseCase = ApplyAnchorOffsetUseCase()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * Verifies that a persisted DAILY fixed-time schedule resolves into one
     * concrete same-day supplement occurrence at the fixed time.
     */
    @Test
    fun persistedDailyFixedSchedule_returnsFixedTime() = runTest {
        val date = LocalDate(2026, 3, 31)

        insertSupplement(
            id = 1L,
            name = "Vitamin C"
        )

        insertFixedSchedule(
            supplementId = 1L,
            recurrenceType = ScheduleRecurrenceType.DAILY,
            interval = 1,
            startDate = LocalDate(2026, 3, 1),
            fixedTimes = listOf(
                fixedTime(
                    time = LocalTime(8, 0),
                    sortOrder = 0,
                    label = "Morning"
                )
            )
        )

        val result = useCase(date).first()

        assertThat(result).hasSize(1)

        val supplement = result.single()
        assertThat(supplement.scheduledTimes).containsExactly(LocalTime(8, 0))
        assertThat(supplement.resolvedScheduleEntries).hasSize(1)

        val entry = supplement.resolvedScheduleEntries.single()
        assertThat(entry.time).isEqualTo(LocalTime(8, 0))
        assertThat(entry.anchor).isNull()
        assertThat(entry.label).isEqualTo("Morning")
    }

    /**
     * Verifies that persisted anchored rows preserve distinct offsets and resolve
     * independently against the same anchor.
     */
    @Test
    fun persistedAnchoredRows_withDifferentOffsets_resolveIndependently() = runTest {
        val date = LocalDate(2026, 3, 31)

        seedDefaultAnchorTime(
            anchor = DoseAnchorType.BREAKFAST,
            time = LocalTime(8, 0)
        )

        insertSupplement(
            id = 2L,
            name = "Magnesium"
        )

        insertAnchoredSchedule(
            supplementId = 2L,
            recurrenceType = ScheduleRecurrenceType.DAILY,
            interval = 1,
            startDate = LocalDate(2026, 3, 1),
            anchoredTimes = listOf(
                anchoredTime(
                    anchor = TimeAnchor.BREAKFAST,
                    offsetMinutes = -15,
                    sortOrder = 0,
                    label = "Before breakfast"
                ),
                anchoredTime(
                    anchor = TimeAnchor.BREAKFAST,
                    offsetMinutes = 30,
                    sortOrder = 1,
                    label = "After breakfast"
                )
            )
        )

        val result = useCase(date).first()

        assertThat(result).hasSize(1)

        val supplement = result.single()
        assertThat(supplement.scheduledTimes)
            .containsExactly(LocalTime(7, 45), LocalTime(8, 30))
            .inOrder()

        assertThat(supplement.resolvedScheduleEntries).hasSize(2)
        assertThat(supplement.resolvedScheduleEntries.map { it.label })
            .containsExactly("Before breakfast", "After breakfast")
            .inOrder()
    }

    /**
     * Verifies that a persisted WEEKLY schedule only appears on matching weekdays.
     */
    @Test
    fun persistedWeeklySchedule_nonMatchingWeekday_doesNotAppear() = runTest {
        val tuesday = LocalDate(2026, 3, 31)

        insertSupplement(
            id = 3L,
            name = "Fish Oil"
        )

        insertFixedSchedule(
            supplementId = 3L,
            recurrenceType = ScheduleRecurrenceType.WEEKLY,
            interval = 1,
            weeklyDays = listOf(DayOfWeek.MONDAY),
            startDate = LocalDate(2026, 3, 1),
            fixedTimes = listOf(
                fixedTime(
                    time = LocalTime(9, 0),
                    sortOrder = 0
                )
            )
        )

        val result = useCase(tuesday).first()

        assertThat(result).isEmpty()
    }

    /**
     * Verifies that a disabled persisted schedule row is ignored even if its
     * date window and recurrence would otherwise match.
     */
    @Test
    fun disabledPersistedSchedule_doesNotAppear() = runTest {
        val date = LocalDate(2026, 3, 31)

        insertSupplement(
            id = 4L,
            name = "Creatine"
        )

        insertFixedSchedule(
            supplementId = 4L,
            recurrenceType = ScheduleRecurrenceType.DAILY,
            interval = 1,
            startDate = LocalDate(2026, 3, 1),
            isEnabled = false,
            fixedTimes = listOf(
                fixedTime(
                    time = LocalTime(10, 0),
                    sortOrder = 0
                )
            )
        )

        val result = useCase(date).first()

        assertThat(result).isEmpty()
    }

    /**
     * Verifies that one supplement can contribute multiple same-day outputs from
     * multiple applicable persisted schedule rows.
     */
    @Test
    fun multiplePersistedSchedulesForOneSupplement_returnMultipleTimes() = runTest {
        val date = LocalDate(2026, 3, 31)

        seedDefaultAnchorTime(
            anchor = DoseAnchorType.LUNCH,
            time = LocalTime(12, 0)
        )

        insertSupplement(
            id = 5L,
            name = "Protein"
        )

        insertFixedSchedule(
            supplementId = 5L,
            recurrenceType = ScheduleRecurrenceType.DAILY,
            interval = 1,
            startDate = LocalDate(2026, 3, 1),
            fixedTimes = listOf(
                fixedTime(
                    time = LocalTime(9, 0),
                    sortOrder = 0,
                    label = "Morning dose"
                )
            )
        )

        insertAnchoredSchedule(
            supplementId = 5L,
            recurrenceType = ScheduleRecurrenceType.DAILY,
            interval = 1,
            startDate = LocalDate(2026, 3, 1),
            anchoredTimes = listOf(
                anchoredTime(
                    anchor = TimeAnchor.LUNCH,
                    offsetMinutes = 15,
                    sortOrder = 0,
                    label = "After lunch"
                )
            )
        )

        val result = useCase(date).first()

        assertThat(result).hasSize(1)

        val supplement = result.single()
        assertThat(supplement.scheduledTimes)
            .containsExactly(LocalTime(9, 0), LocalTime(12, 15))
            .inOrder()

        assertThat(supplement.resolvedScheduleEntries).hasSize(2)
    }

    private suspend fun insertSupplement(
        id: Long,
        name: String,
        doseAnchorType: DoseAnchorType = DoseAnchorType.ANYTIME
    ) {
        db.supplementEntityDao().insertSupplement(
            SupplementEntity(
                id = id,
                name = name,
                doseAnchorType = doseAnchorType,
                servingsPerDay = 1.0,
                frequencyType = FrequencyType.DAILY,
                brand = "Brand X",
                notes = null,
                recommendedServingSize = 1.0,
                recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
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
                isActive = true
            )
        )
    }

    private suspend fun seedDefaultAnchorTime(
        anchor: DoseAnchorType,
        time: LocalTime
    ) {
        db.eventTimeDao().upsertDefault(
            EventDefaultTimeEntity(
                anchor = anchor,
                timeSeconds = time.toSecondOfDay()
            )
        )
    }

    private suspend fun insertFixedSchedule(
        supplementId: Long,
        recurrenceType: ScheduleRecurrenceType,
        interval: Int,
        startDate: LocalDate,
        fixedTimes: List<SupplementScheduleFixedTimeEntity>,
        weeklyDays: List<DayOfWeek>? = null,
        endDate: LocalDate? = null,
        isEnabled: Boolean = true
    ): Long {
        val scheduleId = db.supplementScheduleDao().insertSchedule(
            SupplementScheduleEntity(
                supplementId = supplementId,
                recurrenceType = recurrenceType,
                interval = interval,
                weeklyDays = weeklyDays,
                startDate = startDate,
                endDate = endDate,
                timingType = ScheduleTimingType.FIXED,
                isEnabled = isEnabled
            )
        )

        db.supplementScheduleDao().insertFixedTimes(
            fixedTimes.map { row ->
                row.copy(
                    id = 0L,
                    scheduleId = scheduleId
                )
            }
        )

        return scheduleId
    }

    private suspend fun insertAnchoredSchedule(
        supplementId: Long,
        recurrenceType: ScheduleRecurrenceType,
        interval: Int,
        startDate: LocalDate,
        anchoredTimes: List<SupplementScheduleAnchoredTimeEntity>,
        weeklyDays: List<DayOfWeek>? = null,
        endDate: LocalDate? = null,
        isEnabled: Boolean = true
    ): Long {
        val scheduleId = db.supplementScheduleDao().insertSchedule(
            SupplementScheduleEntity(
                supplementId = supplementId,
                recurrenceType = recurrenceType,
                interval = interval,
                weeklyDays = weeklyDays,
                startDate = startDate,
                endDate = endDate,
                timingType = ScheduleTimingType.ANCHORED,
                isEnabled = isEnabled
            )
        )

        db.supplementScheduleDao().insertAnchoredTimes(
            anchoredTimes.map { row ->
                row.copy(
                    id = 0L,
                    scheduleId = scheduleId
                )
            }
        )

        return scheduleId
    }

    private fun fixedTime(
        time: LocalTime,
        sortOrder: Int,
        label: String? = null
    ): SupplementScheduleFixedTimeEntity {
        return SupplementScheduleFixedTimeEntity(
            id = 0L,
            scheduleId = 0L,
            time = time,
            label = label,
            sortOrder = sortOrder
        )
    }

    private fun anchoredTime(
        anchor: TimeAnchor,
        offsetMinutes: Int,
        sortOrder: Int,
        label: String? = null
    ): SupplementScheduleAnchoredTimeEntity {
        return SupplementScheduleAnchoredTimeEntity(
            id = 0L,
            scheduleId = 0L,
            anchor = anchor,
            offsetMinutes = offsetMinutes,
            label = label,
            sortOrder = sortOrder
        )
    }

    /**
     * Minimal fake used because this test suite is focused on supplement schedule
     * persistence + resolution. Workout-aware coverage can be added later by
     * populating [activitiesForDate].
     */
    private class FakeActivityRepository : ActivityRepository {
        private val activitiesForDate = MutableStateFlow<List<Activity>>(emptyList())

        override fun observeAll(): Flow<List<Activity>> = activitiesForDate

        override fun observeActivity(id: Long): Flow<Activity?> =
            MutableStateFlow(activitiesForDate.value.firstOrNull { it.id == id })

        override suspend fun addActivity(activity: Activity): Long {
            activitiesForDate.value = activitiesForDate.value + activity
            return activity.id
        }

        override suspend fun deleteActivity(activity: Activity) {
            activitiesForDate.value = activitiesForDate.value.filterNot { it.id == activity.id }
        }

        override fun observeActivitiesForDate(date: LocalDate): Flow<List<Activity>> {
            return MutableStateFlow(
                activitiesForDate.value.filter { it.start.date == date }
            )
        }

        override suspend fun insertActivity(
            type: ActivityType,
            startTimestamp: Long,
            endTimestamp: Long?,
            notes: String?,
            intensity: Int?
        ): Long {
            throw UnsupportedOperationException(
                "insertActivity is not needed for this test fake."
            )
        }
    }
}