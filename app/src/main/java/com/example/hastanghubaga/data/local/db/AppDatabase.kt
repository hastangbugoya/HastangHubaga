package com.example.hastanghubaga.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hastanghubaga.data.local.converters.Converters
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityOccurrenceDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityScheduleDao
import com.example.hastanghubaga.data.local.dao.meal.AkImportedLogDao
import com.example.hastanghubaga.data.local.dao.meal.AkImportedMealDao
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
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
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedLogEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDayOfWeekTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementIngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity
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
        MealEntity::class,
        MealNutritionEntity::class,
        AkImportedLogEntity::class,
        AkImportedMealEntity::class,
        UserNutritionGoalsEntity::class,
        UpcomingScheduleEntity::class,
        UserNutritionPlanEntity::class,
        NutrientGoalEntity::class,
        IngredientPreferenceEntity::class,
    ],
    version = 7,
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

    abstract fun mealEntityDao(): MealEntityDao
    abstract fun mealNutritionDao(): MealNutritionDao
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