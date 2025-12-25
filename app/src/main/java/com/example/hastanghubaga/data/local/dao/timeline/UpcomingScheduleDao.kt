package com.example.hastanghubaga.data.local.dao.timeline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UpcomingScheduleDao {

    /** Clear and repopulate atomically */
    @Transaction
    suspend fun replaceAll(items: List<UpcomingScheduleEntity>) {
        clearAll()
        insertAll(items)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UpcomingScheduleEntity>)

    @Query("DELETE FROM upcoming_schedule")
    suspend fun clearAll()

    /** Used by widget + alerts */
    @Query("""
        SELECT * FROM upcoming_schedule
        WHERE scheduledAt >= :fromUtc
        ORDER BY scheduledAt ASC
    """)
    fun observeUpcoming(
        fromUtc: Long
    ): Flow<List<UpcomingScheduleEntity>>

    @Query("SELECT * FROM upcoming_schedule ORDER BY scheduledAt ASC")
    fun observeAll(): Flow<List<UpcomingScheduleEntity>>

    @Query("""
    SELECT *
    FROM upcoming_schedule
    WHERE isCompleted = 0
      AND scheduledAt > :now
    ORDER BY scheduledAt ASC
    LIMIT 1
""")
    fun observeNextUpcoming(now: LocalDateTime): Flow<UpcomingScheduleEntity?>

}
