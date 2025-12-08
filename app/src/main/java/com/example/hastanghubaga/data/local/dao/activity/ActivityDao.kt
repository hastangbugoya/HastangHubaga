package com.example.hastanghubaga.data.local.dao.activity

import androidx.room.*
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities ORDER BY startTimestamp DESC")
    fun observeAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    fun observeActivity(id: Long): Flow<ActivityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(entity: ActivityEntity): Long

    @Delete
    suspend fun deleteActivity(entity: ActivityEntity)

    @Query("DELETE FROM activities")
    suspend fun clearAll()
}
