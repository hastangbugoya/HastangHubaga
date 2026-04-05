package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealOccurrenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOccurrence(entity: MealOccurrenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOccurrences(entities: List<MealOccurrenceEntity>)

    @Query("""
        SELECT * FROM meal_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
    """)
    fun observeOccurrencesForDate(date: String): Flow<List<MealOccurrenceEntity>>

    @Query("""
        SELECT * FROM meal_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
    """)
    suspend fun getOccurrencesForDate(date: String): List<MealOccurrenceEntity>

    @Query("""
        SELECT * FROM meal_occurrences
        WHERE id = :occurrenceId
        LIMIT 1
    """)
    suspend fun getOccurrenceById(occurrenceId: String): MealOccurrenceEntity?

    @Query("""
        DELETE FROM meal_occurrences
        WHERE date = :date
          AND sourceType = 'SCHEDULED'
    """)
    suspend fun deleteScheduledOccurrencesForDate(date: String)

    @Query("""
        UPDATE meal_occurrences
        SET isDeleted = 1
        WHERE id = :occurrenceId
    """)
    suspend fun softDeleteOccurrence(occurrenceId: String)

    @Query("""
        UPDATE meal_occurrences
        SET isDeleted = 0
        WHERE id = :occurrenceId
    """)
    suspend fun restoreOccurrence(occurrenceId: String)
}