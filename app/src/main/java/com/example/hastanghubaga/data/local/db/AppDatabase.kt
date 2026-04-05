package com.example.hastanghubaga.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hastanghubaga.data.local.converters.Converters
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityLogDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityOccurrenceDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityScheduleDao
import com.example.hastanghubaga.data.local.dao.meal.AkImportedLogDao
import com.example.hastanghubaga.data.local.dao.meal.AkImportedMealDao
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.dao.meal.MealScheduleDao
import com.example.hastanghubaga.data.local.dao.meal.MealOccurrenceDao
import com.example.hastanghubaga.data.local.dao.meal.MealLogDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementIngredientDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementNutritionDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementOccurrenceDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementScheduleDao
import com.example.hastanghubaga.data.local.dao.timeline.UpcomingScheduleDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.dao.widget.IngredientPreferenceDao
import com.example.hastanghubaga.data.local.entity.activity.*
import com.example.hastanghubaga.data.local.entity.meal.*
import com.example.hastanghubaga.data.local.entity.supplement.*
import com.example.hastanghubaga.data.local.entity.user.*
import com.example.hastanghubaga.data.local.entity.widget.IngredientPreferenceEntity

@Database(
    entities = [
        IngredientEntity::class,
        SupplementEntity::class,
        SupplementDailyLogEntity::class,
        SupplementOccurrenceEntity::class,
        DailyStartTimeEntity::class,
        SupplementIngredientEntity::class,
        EventDefaultTimeEntity::class,
        EventDayOfWeekTimeEntity::class,
        EventDailyOverrideEntity::class,
        SupplementUserSettingsEntity::class,
        SupplementScheduleEntity::class,
        SupplementScheduleFixedTimeEntity::class,
        SupplementScheduleAnchoredTimeEntity::class,

        ActivityEntity::class,
        ActivityScheduleEntity::class,
        ActivityScheduleFixedTimeEntity::class,
        ActivityScheduleAnchoredTimeEntity::class,
        ActivityOccurrenceEntity::class,
        ActivityLogEntity::class,

        MealEntity::class,
        MealScheduleEntity::class,
        MealScheduleFixedTimeEntity::class,
        MealScheduleAnchoredTimeEntity::class,
        MealOccurrenceEntity::class,
        MealNutritionEntity::class,
        MealLogEntity::class, // ✅ ADD

        AkImportedLogEntity::class,
        AkImportedMealEntity::class,

        UserNutritionGoalsEntity::class,
        UpcomingScheduleEntity::class,
        UserNutritionPlanEntity::class,
        NutrientGoalEntity::class,
        IngredientPreferenceEntity::class,
    ],
    version = 14, // ⚠️ bump version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ingredientEntityDao(): IngredientEntityDao
    abstract fun supplementEntityDao(): SupplementEntityDao
    abstract fun supplementDailyLogDao(): SupplementDailyLogDao
    abstract fun supplementOccurrenceDao(): SupplementOccurrenceDao
    abstract fun dailyStartTimeDao(): DailyStartTimeDao
    abstract fun supplementIngredientDao(): SupplementIngredientDao
    abstract fun eventTimeDao(): EventTimeDao
    abstract fun supplementUserSettingsDao(): SupplementUserSettingsDao
    abstract fun supplementScheduleDao(): SupplementScheduleDao

    abstract fun activityEntityDao(): ActivityEntityDao
    abstract fun activityScheduleDao(): ActivityScheduleDao
    abstract fun activityOccurrenceDao(): ActivityOccurrenceDao
    abstract fun activityLogDao(): ActivityLogDao

    abstract fun mealEntityDao(): MealEntityDao
    abstract fun mealScheduleDao(): MealScheduleDao
    abstract fun mealNutritionDao(): MealNutritionDao
    abstract fun mealOccurrenceDao(): MealOccurrenceDao
    abstract fun mealLogDao(): MealLogDao // ✅ ADD

    abstract fun akImportedLogDao(): AkImportedLogDao
    abstract fun akImportedMealDao(): AkImportedMealDao
    abstract fun userNutritionGoalsEntityDao(): UserNutritionGoalsEntityDao
    abstract fun upcomingScheduleDao(): UpcomingScheduleDao
    abstract fun nutritionPlanEntityDao(): NutritionPlanEntityDao
    abstract fun nutrientGoalDao(): NutrientGoalDao
    abstract fun ingredientPreferenceDao(): IngredientPreferenceDao
    abstract fun supplementNutritionDao(): SupplementNutritionDao

    companion object
}