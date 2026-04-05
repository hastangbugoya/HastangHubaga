package com.example.hastanghubaga.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityLogDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityOccurrenceDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityScheduleDao
import com.example.hastanghubaga.data.local.dao.meal.AkImportedLogDao
import com.example.hastanghubaga.data.local.dao.meal.AkImportedMealDao
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealLogDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.dao.meal.MealOccurrenceDao
import com.example.hastanghubaga.data.local.dao.meal.MealScheduleDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementNutritionDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementOccurrenceDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementScheduleDao
import com.example.hastanghubaga.data.local.dao.timeline.UpcomingScheduleDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.dao.widget.IngredientPreferenceDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.db.callback.DatabaseCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context,
        callback: DatabaseCallback
    ): AppDatabase =
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "hastanghubaga-db"
        )
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
            .also {
                Log.e("Meow", "DB INSTANCE CREATED")
            }

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
    @Singleton
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
    fun provideActivityScheduleDao(db: AppDatabase): ActivityScheduleDao =
        db.activityScheduleDao()

    @Provides
    fun provideActivityOccurrenceDao(db: AppDatabase): ActivityOccurrenceDao =
        db.activityOccurrenceDao()

    @Provides
    fun provideActivityLogDao(db: AppDatabase): ActivityLogDao =
        db.activityLogDao()

    @Provides
    fun provideMealEntityDao(db: AppDatabase): MealEntityDao =
        db.mealEntityDao()

    @Provides
    fun provideMealScheduleDao(db: AppDatabase): MealScheduleDao =
        db.mealScheduleDao()

    @Provides
    fun provideMealNutritionDao(db: AppDatabase): MealNutritionDao =
        db.mealNutritionDao()

    @Provides
    fun provideMealLogDao(db: AppDatabase): MealLogDao =
        db.mealLogDao()

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

    @Provides
    fun provideSupplementOccurrenceDao(db: AppDatabase): SupplementOccurrenceDao =
        db.supplementOccurrenceDao()

    @Provides
    fun provideMealOccurrenceDao(db: AppDatabase): MealOccurrenceDao =
        db.mealOccurrenceDao()
}