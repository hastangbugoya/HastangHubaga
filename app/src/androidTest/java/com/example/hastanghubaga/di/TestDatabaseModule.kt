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
import com.example.hastanghubaga.data.local.dao.supplement.SupplementIngredientDao
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.dao.user.UserNutritionGoalsEntityDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * app/src/androidTest/java/com/example/hastanghubaga/di/TestDatabaseModule.kt
 *
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
 * DAO provider method names MUST MATCH the production module.
 * Hilt generates factories based on method signatures.
 *
 * CHECKLIST (MUST VERIFY)
 * -----------------------
 * ✓ Annotated with @TestInstallIn
 * ✓ Replaces DatabaseModule
 * ✓ Uses Room.inMemoryDatabaseBuilder
 * ✓ All DAO providers exist and are named identically to production
 * ✓ No duplicate TestDatabaseModule elsewhere
 *
 * COMMON MISTAKES
 * ---------------
 * ✗ Missing a DAO provider → kapt “cannot find symbol” error
 * ✗ Method name mismatch (provideXDao vs provideXEntityDao)
 * ✗ Keeping multiple TestDatabaseModule files
 *
 * TIPS
 * ----
 * • When a kapt error mentions ProvideXDaoFactory → this file is the issue
 * • If you add a new DAO, update BOTH DatabaseModule and TestDatabaseModule
 * • Always clean & rebuild after changing this file
 */

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    // -------------------------
    // DATABASE
    // -------------------------
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

    // -------------------------
    // DAOs — MUST MATCH NAMES
    // -------------------------

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
    fun provideUserNutritionGoalsEntityDao(db: AppDatabase): UserNutritionGoalsEntityDao =
        db.userNutritionGoalsEntityDao()

    @Provides
    fun provideSupplementIngredientDao(db: AppDatabase): SupplementIngredientDao =
        db.supplementIngredientDao()
}