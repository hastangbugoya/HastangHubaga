package com.example.hastanghubaga.model.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.hastanghubaga.model.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.model.entity.supplement.IngredientEntity
import com.example.hastanghubaga.model.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.model.entity.supplement.SupplementEntity

@Database(
    entities = [
        IngredientEntity::class,
        SupplementEntity::class,
        SupplementDailyLogEntity::class,
        DailyStartTimeEntity::class
        // add others here later
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun supplementEntityDao(): SupplementEntityDao
    abstract fun supplementDailyLogEntity(): SupplementDailyLogEntity
    abstract fun dailyStartTimeDao(): DailyStartTimeDao
}
