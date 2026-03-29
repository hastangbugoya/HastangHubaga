package com.example.hastanghubaga.data.local.dao.schedule

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleOwnerType
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleWeeklyDayEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleWithDetails

@Dao
interface ScheduleRuleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertScheduleRule(rule: ScheduleRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyDays(days: List<ScheduleRuleWeeklyDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTimes(times: List<ScheduleRuleFixedTimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTimes(times: List<ScheduleRuleAnchoredTimeEntity>)

    @Query(
        """
        SELECT * 
        FROM schedule_rules
        WHERE ownerType = :ownerType AND ownerId = :ownerId
        LIMIT 1
        """
    )
    suspend fun getScheduleRule(
        ownerType: ScheduleOwnerType,
        ownerId: Long
    ): ScheduleRuleEntity?

    @Transaction
    @Query(
        """
        SELECT * 
        FROM schedule_rules
        WHERE ownerType = :ownerType AND ownerId = :ownerId
        LIMIT 1
        """
    )
    suspend fun getScheduleRuleWithDetails(
        ownerType: ScheduleOwnerType,
        ownerId: Long
    ): ScheduleRuleWithDetails?

    @Transaction
    @Query(
        """
        SELECT * 
        FROM schedule_rules
        WHERE id = :scheduleRuleId
        LIMIT 1
        """
    )
    suspend fun getScheduleRuleWithDetailsById(
        scheduleRuleId: Long
    ): ScheduleRuleWithDetails?

    @Query(
        """
        DELETE FROM schedule_rule_weekly_days
        WHERE scheduleRuleId = :scheduleRuleId
        """
    )
    suspend fun deleteWeeklyDaysForRule(scheduleRuleId: Long)

    @Query(
        """
        DELETE FROM schedule_rule_fixed_times
        WHERE scheduleRuleId = :scheduleRuleId
        """
    )
    suspend fun deleteFixedTimesForRule(scheduleRuleId: Long)

    @Query(
        """
        DELETE FROM schedule_rule_anchored_times
        WHERE scheduleRuleId = :scheduleRuleId
        """
    )
    suspend fun deleteAnchoredTimesForRule(scheduleRuleId: Long)

    @Query(
        """
        DELETE FROM schedule_rules
        WHERE ownerType = :ownerType AND ownerId = :ownerId
        """
    )
    suspend fun deleteScheduleRule(
        ownerType: ScheduleOwnerType,
        ownerId: Long
    )

    @Transaction
    suspend fun replaceScheduleRule(
        rule: ScheduleRuleEntity,
        weeklyDays: List<ScheduleRuleWeeklyDayEntity>,
        fixedTimes: List<ScheduleRuleFixedTimeEntity>,
        anchoredTimes: List<ScheduleRuleAnchoredTimeEntity>
    ): Long {
        val existing = getScheduleRule(rule.ownerType, rule.ownerId)
        if (existing != null) {
            deleteWeeklyDaysForRule(existing.id)
            deleteFixedTimesForRule(existing.id)
            deleteAnchoredTimesForRule(existing.id)
            deleteScheduleRule(rule.ownerType, rule.ownerId)
        }

        val newRuleId = insertScheduleRule(rule)

        if (weeklyDays.isNotEmpty()) {
            insertWeeklyDays(
                weeklyDays.map { it.copy(scheduleRuleId = newRuleId) }
            )
        }

        if (fixedTimes.isNotEmpty()) {
            insertFixedTimes(
                fixedTimes.map { it.copy(scheduleRuleId = newRuleId) }
            )
        }

        if (anchoredTimes.isNotEmpty()) {
            insertAnchoredTimes(
                anchoredTimes.map { it.copy(scheduleRuleId = newRuleId) }
            )
        }

        return newRuleId
    }
}