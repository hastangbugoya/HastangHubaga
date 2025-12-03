package com.example.hastanghubaga.model.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hastanghubaga.model.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.model.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.model.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.model.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.model.dao.supplement.SupplementIngredientDao
import com.example.hastanghubaga.model.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.model.entity.supplement.IngredientEntity
import com.example.hastanghubaga.model.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.model.entity.supplement.SupplementEntity
import com.example.hastanghubaga.model.entity.supplement.SupplementIngredientEntity

@Database(
    entities = [
        IngredientEntity::class,
        SupplementEntity::class,
        SupplementDailyLogEntity::class,
        DailyStartTimeEntity::class,
        SupplementIngredientEntity::class
        // add others here later
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientEntityDao(): IngredientEntityDao
    abstract fun supplementEntityDao(): SupplementEntityDao
    abstract fun supplementDailyLogDao(): SupplementDailyLogDao
    abstract fun dailyStartTimeDao(): DailyStartTimeDao
    abstract fun supplementIngredientDao(): SupplementIngredientDao
}
