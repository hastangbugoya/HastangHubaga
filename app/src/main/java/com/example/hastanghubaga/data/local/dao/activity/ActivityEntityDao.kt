package com.example.hastanghubaga.data.local.dao.activity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityEntityDao {

    @Query("SELECT * FROM activities ORDER BY startTimestamp DESC")
    suspend fun getAllActivities(): List<ActivityEntity>

    @Query("SELECT * FROM activities ORDER BY startTimestamp DESC")
    fun observeAllActivities(): Flow<List<ActivityEntity>>

    /**
     * Observe active activity templates/rows that are eligible for manual
     * force-log selection from Today screen.
     *
     * Important:
     * - Active only
     * - Not date-scoped
     * - Does not merge scheduled occurrences
     *
     * This keeps the force-log picker distinct from the timeline's
     * planned-occurrence query path.
     */
    @Query(
        """
        SELECT * FROM activities
        WHERE isActive = 1
        ORDER BY startTimestamp DESC
    """
    )
    fun observeActiveActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    fun observeActivity(id: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    suspend fun getActivityById(id: Long): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(entity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(entity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(entity: ActivityEntity)

    @Query("DELETE FROM activities")
    suspend fun clearAll()

    @Query(
        """
        SELECT * FROM activities a
        WHERE a.isActive = 1
          AND a.startTimestamp >= :start
          AND a.startTimestamp < :end
          AND NOT EXISTS (
              SELECT 1
              FROM activity_schedules s
              WHERE s.activityId = a.id
                AND s.isEnabled = 1
          )
        ORDER BY a.startTimestamp ASC
    """
    )
    fun observeActivitiesForDay(
        start: Long,
        end: Long
    ): Flow<List<ActivityEntity>>
}