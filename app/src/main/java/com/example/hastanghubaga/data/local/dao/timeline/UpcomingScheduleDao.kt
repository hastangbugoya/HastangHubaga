package com.example.hastanghubaga.data.local.dao.timeline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UpcomingScheduleDao {

    /* -------------------------------------------------- */
    /* Writes                                              */
    /* -------------------------------------------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UpcomingScheduleEntity>)

    @Query("DELETE FROM upcoming_schedule WHERE scheduledAt >= :fromUtc")
    suspend fun clearFrom(fromUtc: Long)

    /* -------------------------------------------------- */
    /* Reads                                               */
    /* -------------------------------------------------- */

    @Query("""
        SELECT * FROM upcoming_schedule
        WHERE scheduledAt >= :nowUtc
        ORDER BY scheduledAt ASC
        LIMIT :limit
    """)
    fun observeUpcoming(
        nowUtc: Long,
        limit: Int
    ): Flow<List<UpcomingScheduleEntity>>

    @Query("""
        SELECT * FROM upcoming_schedule
        WHERE scheduledAt >= :nowUtc
        ORDER BY scheduledAt ASC
        LIMIT 1
    """)
    suspend fun getNextUpcoming(nowUtc: Long): UpcomingScheduleEntity?
}
