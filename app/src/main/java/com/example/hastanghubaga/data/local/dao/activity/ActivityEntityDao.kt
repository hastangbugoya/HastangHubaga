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
        SELECT * FROM activities
        WHERE startTimestamp >= :start
          AND startTimestamp < :end
        ORDER BY startTimestamp ASC
    """
    )
    fun observeActivitiesForDay(
        start: Long,
        end: Long
    ): Flow<List<ActivityEntity>>
}