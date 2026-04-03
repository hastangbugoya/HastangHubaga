package com.example.hastanghubaga.data.local.dao.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityOccurrenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOccurrence(entity: ActivityOccurrenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOccurrences(entities: List<ActivityOccurrenceEntity>)

    @Query("""
        SELECT * FROM activity_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
    """)
    fun observeOccurrencesForDate(date: String): Flow<List<ActivityOccurrenceEntity>>

    @Query("""
        SELECT * FROM activity_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
    """)
    suspend fun getOccurrencesForDate(date: String): List<ActivityOccurrenceEntity>

    @Query("""
        SELECT * FROM activity_occurrences
        WHERE date = :date
          AND isDeleted = 0
          AND isWorkout = 1
        ORDER BY plannedTimeSeconds ASC, id ASC
    """)
    suspend fun getWorkoutOccurrencesForDate(date: String): List<ActivityOccurrenceEntity>

    @Query("""
        SELECT * FROM activity_occurrences
        WHERE id = :occurrenceId
        LIMIT 1
    """)
    suspend fun getOccurrenceById(occurrenceId: String): ActivityOccurrenceEntity?

    @Query("""
        DELETE FROM activity_occurrences
        WHERE date = :date
          AND sourceType = 'SCHEDULED'
    """)
    suspend fun deleteScheduledOccurrencesForDate(date: String)

    @Query("""
        UPDATE activity_occurrences
        SET isDeleted = 1
        WHERE id = :occurrenceId
    """)
    suspend fun softDeleteOccurrence(occurrenceId: String)

    @Query("""
        UPDATE activity_occurrences
        SET isDeleted = 0
        WHERE id = :occurrenceId
    """)
    suspend fun restoreOccurrence(occurrenceId: String)

    @Query("""
        UPDATE activity_occurrences
        SET isWorkout = :isWorkout
        WHERE id = :occurrenceId
    """)
    suspend fun updateOccurrenceWorkoutFlag(
        occurrenceId: String,
        isWorkout: Boolean
    )
}