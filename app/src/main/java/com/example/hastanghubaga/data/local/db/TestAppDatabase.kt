package com.example.hastanghubaga.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hastanghubaga.data.local.converters.TestConverters
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity

@Database(
    entities = [SupplementUserSettingsEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TestConverters::class)
abstract class TestAppDatabase : RoomDatabase() {
    abstract fun supplementUserSettingsDao(): SupplementUserSettingsDao
}


///**
// * Full schema **matching AppDatabase** but WITHOUT the callback.
// * Required for Room Flow + invalidation + foreign keys to work during tests.
// */
//@Database(
//    entities = [
//        IngredientEntity::class,
//        SupplementEntity::class,
//        SupplementDailyLogEntity::class,
//        DailyStartTimeEntity::class,
//        SupplementIngredientEntity::class,
//        EventDefaultTimeEntity::class,
//        EventDailyOverrideEntity::class,
//        SupplementUserSettingsEntity::class,
//        ActivityEntity::class,
//        MealEntity::class,
//        MealNutritionEntity::class,
//        UserNutritionGoalsEntity::class
//    ],
//    version = 1,          // version does not matter for in-memory DB
//    exportSchema = false
//)
//@TypeConverters(Converters::class)
//abstract class TestAppDatabase : RoomDatabase() {
//
//    // ---- DAO Exports ----
//    abstract fun ingredientEntityDao(): IngredientEntityDao
//    abstract fun supplementEntityDao(): SupplementEntityDao
//    abstract fun supplementDailyLogDao(): SupplementDailyLogDao
//    abstract fun dailyStartTimeDao(): DailyStartTimeDao
//    abstract fun supplementIngredientDao(): SupplementIngredientDao
//    abstract fun eventTimeDao(): EventTimeDao
//    abstract fun supplementUserSettingsDao(): SupplementUserSettingsDao
//    abstract fun activityEntityDao(): ActivityEntityDao
//    abstract fun mealEntityDao(): MealEntityDao
//    abstract fun mealNutritionDao(): MealNutritionDao
//    abstract fun userNutritionGoalsEntityDao(): UserNutritionGoalsEntityDao
//
//    companion object
//}
