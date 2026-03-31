package com.example.hastanghubaga.di

import android.content.Context
import androidx.room.Room
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
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
import com.example.hastanghubaga.data.local.dao.supplement.SupplementNutritionDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementScheduleDao
import com.example.hastanghubaga.data.local.dao.timeline.UpcomingScheduleDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.dao.widget.IngredientPreferenceDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Hilt test module that replaces the production DatabaseModule.
 *
 * PURPOSE
 * -------
 * Supplies an in-memory Room database and DAOs for Android tests.
 *
 * This module uses @TestInstallIn to:
 * - Replace the production DatabaseModule
 * - Ensure all DAOs resolve correctly during tests
 * - Avoid touching disk or production schema
 *
 * CRITICAL RULE
 * -------------
 * DAO provider coverage must stay in sync with the production DatabaseModule.
 *
 * If you add a new DAO to DatabaseModule, add the matching test provider here too.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideSupplementEntityDao(db: AppDatabase): SupplementEntityDao =
        db.supplementEntityDao()

    @Provides
    fun provideSupplementScheduleDao(db: AppDatabase): SupplementScheduleDao =
        db.supplementScheduleDao()

    @Provides
    fun provideIngredientDao(db: AppDatabase): IngredientEntityDao =
        db.ingredientEntityDao()

    @Provides
    fun provideSupplementDailyLogDao(db: AppDatabase): SupplementDailyLogDao =
        db.supplementDailyLogDao()

    @Provides
    fun provideDailyStartTimeDao(db: AppDatabase): DailyStartTimeDao =
        db.dailyStartTimeDao()

    @Provides
    fun provideEventTimeDao(db: AppDatabase): EventTimeDao =
        db.eventTimeDao()

    @Provides
    fun provideSupplementUserSettingsDao(db: AppDatabase): SupplementUserSettingsDao =
        db.supplementUserSettingsDao()

    @Provides
    fun provideActivityEntityDao(db: AppDatabase): ActivityEntityDao =
        db.activityEntityDao()

    @Provides
    fun provideMealEntityDao(db: AppDatabase): MealEntityDao =
        db.mealEntityDao()

    @Provides
    fun provideMealNutritionDao(db: AppDatabase): MealNutritionDao =
        db.mealNutritionDao()

    @Provides
    fun provideAkImportedLogDao(db: AppDatabase): AkImportedLogDao =
        db.akImportedLogDao()

    @Provides
    fun provideAkImportedMealDao(db: AppDatabase): AkImportedMealDao =
        db.akImportedMealDao()

    @Provides
    fun provideUserNutritionGoalsEntityDao(db: AppDatabase): UserNutritionGoalsEntityDao =
        db.userNutritionGoalsEntityDao()

    @Provides
    fun provideUpcomingScheduleDao(db: AppDatabase): UpcomingScheduleDao =
        db.upcomingScheduleDao()

    @Provides
    fun provideNutritionPlanEntityDao(db: AppDatabase): NutritionPlanEntityDao =
        db.nutritionPlanEntityDao()

    @Provides
    fun provideNutrientGoalDao(db: AppDatabase): NutrientGoalDao =
        db.nutrientGoalDao()

    @Provides
    fun provideIngredientPreferenceDao(db: AppDatabase): IngredientPreferenceDao =
        db.ingredientPreferenceDao()

    @Provides
    fun provideSupplementNutritionDao(db: AppDatabase): SupplementNutritionDao =
        db.supplementNutritionDao()
}