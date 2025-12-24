package com.example.hastanghubaga.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hastanghubaga.data.local.converters.Converters
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementIngredientDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementIngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.dao.timeline.UpcomingScheduleDao
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.entity.supplement.EventDayOfWeekTimeEntity
import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity


@Database(
    entities = [
        IngredientEntity::class,
        SupplementEntity::class,
        SupplementDailyLogEntity::class,
        DailyStartTimeEntity::class,
        SupplementIngredientEntity::class,
        EventDefaultTimeEntity::class,
        EventDayOfWeekTimeEntity::class,
        EventDailyOverrideEntity::class,
        SupplementUserSettingsEntity::class,
        ActivityEntity::class,
        MealEntity::class,
        MealNutritionEntity::class,
        UserNutritionGoalsEntity::class,
        UpcomingScheduleEntity::class
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
    abstract fun eventTimeDao(): EventTimeDao
    abstract fun supplementUserSettingsDao(): SupplementUserSettingsDao
    abstract fun activityEntityDao(): ActivityEntityDao
    abstract fun mealEntityDao(): MealEntityDao
    abstract fun mealNutritionDao(): MealNutritionDao
    abstract fun userNutritionGoalsEntityDao(): UserNutritionGoalsEntityDao
    abstract fun upcomingScheduleDao(): UpcomingScheduleDao


    companion object

}
