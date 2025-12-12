package com.example.hastanghubaga.di

import android.content.Context
import androidx.room.Room
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
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

    @Provides
    fun provideSupplementEntityDao(db: AppDatabase): SupplementEntityDao =
        db.supplementEntityDao()
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
    fun provideMealEntityDao(db: AppDatabase): MealEntityDao =
        db.mealEntityDao()

    @Provides
    fun provideUserNutritionGoalsEntityDao(db: AppDatabase): UserNutritionGoalsEntityDao =
        db.userNutritionGoalsEntityDao()

    @Provides
    fun provideMealNutritionDao(db: AppDatabase): MealNutritionDao =
        db.mealNutritionDao()

}
