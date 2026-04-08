package com.example.hastanghubaga.data.local.dao.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.models.ActivityOccurrenceWithActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityOccurrenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOccurrence(entity: ActivityOccurrenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOccurrences(entities: List<ActivityOccurrenceEntity>)

    @Query(
        """
        SELECT
            o.id AS occ_id,
            o.activityId AS occ_activityId,
            o.scheduleId AS occ_scheduleId,
            o.date AS occ_date,
            o.plannedTimeSeconds AS occ_plannedTimeSeconds,
            o.sourceType AS occ_sourceType,
            o.isDeleted AS occ_isDeleted,
            o.isWorkout AS occ_isWorkout,
            o.title AS occ_title,
            o.savedAddressId AS occ_savedAddressId,
            o.addressAsRawString AS occ_addressAsRawString,
            o.addressDisplayText AS occ_addressDisplayText,

            a.id AS act_id,
            a.title AS act_title,
            a.type AS act_type,
            a.startTimestamp AS act_startTimestamp,
            a.endTimestamp AS act_endTimestamp,
            a.notes AS act_notes,
            a.intensity AS act_intensity,
            a.isWorkout AS act_isWorkout,
            a.isActive AS act_isActive,
            a.sendAlert AS act_sendAlert,
            a.alertOffsetMinutes AS act_alertOffsetMinutes,
            a.savedAddressId AS act_savedAddressId,
            a.addressAsRawString AS act_addressAsRawString
        FROM activity_occurrences o
        INNER JOIN activities a
            ON a.id = o.activityId
        WHERE o.date = :date
          AND o.isDeleted = 0
          AND a.isActive = 1
        ORDER BY o.plannedTimeSeconds ASC, o.id ASC
        """
    )
    fun observeOccurrencesWithActivityForDate(
        date: String
    ): Flow<List<ActivityOccurrenceWithActivity>>

    @Query(
        """
        SELECT * FROM activity_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
        """
    )
    fun observeOccurrencesForDate(date: String): Flow<List<ActivityOccurrenceEntity>>

    @Query(
        """
        SELECT * FROM activity_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
        """
    )
    suspend fun getOccurrencesForDate(date: String): List<ActivityOccurrenceEntity>

    @Query(
        """
        SELECT * FROM activity_occurrences
        WHERE date = :date
          AND isDeleted = 0
          AND isWorkout = 1
        ORDER BY plannedTimeSeconds ASC, id ASC
        """
    )
    suspend fun getWorkoutOccurrencesForDate(date: String): List<ActivityOccurrenceEntity>

    @Query(
        """
        SELECT * FROM activity_occurrences
        WHERE id = :occurrenceId
        LIMIT 1
        """
    )
    suspend fun getOccurrenceById(occurrenceId: String): ActivityOccurrenceEntity?

    @Query(
        """
        DELETE FROM activity_occurrences
        WHERE date = :date
          AND sourceType = 'SCHEDULED'
        """
    )
    suspend fun deleteScheduledOccurrencesForDate(date: String)

    @Query(
        """
        UPDATE activity_occurrences
        SET isDeleted = 1
        WHERE id = :occurrenceId
        """
    )
    suspend fun softDeleteOccurrence(occurrenceId: String)

    @Query(
        """
        UPDATE activity_occurrences
        SET isDeleted = 0
        WHERE id = :occurrenceId
        """
    )
    suspend fun restoreOccurrence(occurrenceId: String)

    @Query(
        """
        UPDATE activity_occurrences
        SET isWorkout = :isWorkout
        WHERE id = :occurrenceId
        """
    )
    suspend fun updateOccurrenceWorkoutFlag(
        occurrenceId: String,
        isWorkout: Boolean
    )
}