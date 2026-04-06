package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.ActivityLogRepositoryImpl
import com.example.hastanghubaga.data.repository.ActivityOccurrenceRepositoryImpl
import com.example.hastanghubaga.data.repository.IngredientRepositoryImpl
import com.example.hastanghubaga.data.repository.MealLogRepositoryImpl
import com.example.hastanghubaga.data.repository.MealOccurrenceRepositoryImpl
import com.example.hastanghubaga.data.repository.NutritionAggregateRepositoryImpl
import com.example.hastanghubaga.data.repository.SupplementOccurrenceRepositoryImpl
import com.example.hastanghubaga.data.repository.SupplementRepositoryImpl
import com.example.hastanghubaga.domain.repository.activity.ActivityLogRepository
import com.example.hastanghubaga.domain.repository.activity.ActivityOccurrenceRepository
import com.example.hastanghubaga.domain.repository.meal.MealLogRepository
import com.example.hastanghubaga.domain.repository.meal.MealOccurrenceRepository
import com.example.hastanghubaga.domain.repository.nutrition.NutritionAggregateRepository
import com.example.hastanghubaga.domain.repository.supplement.IngredientRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogReadRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementOccurrenceRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSupplementRepository(
        impl: SupplementRepositoryImpl
    ): SupplementRepository

    @Binds
    @Singleton
    abstract fun bindSupplementDoseLogReadRepository(
        impl: SupplementRepositoryImpl
    ): SupplementDoseLogReadRepository

    @Binds
    @Singleton
    abstract fun bindSupplementOccurrenceRepository(
        impl: SupplementOccurrenceRepositoryImpl
    ): SupplementOccurrenceRepository

    @Binds
    @Singleton
    abstract fun bindIngredientRepository(
        impl: IngredientRepositoryImpl
    ): IngredientRepository

    @Binds
    @Singleton
    abstract fun bindActivityOccurrenceRepository(
        impl: ActivityOccurrenceRepositoryImpl
    ): ActivityOccurrenceRepository

    @Binds
    @Singleton
    abstract fun bindActivityLogRepository(
        impl: ActivityLogRepositoryImpl
    ): ActivityLogRepository

    @Binds
    @Singleton
    abstract fun bindMealOccurrenceRepository(
        impl: MealOccurrenceRepositoryImpl
    ): MealOccurrenceRepository

    @Binds
    @Singleton
    abstract fun bindMealLogRepository(
        impl: MealLogRepositoryImpl
    ): MealLogRepository

    @Binds
    @Singleton
    abstract fun bindNutritionAggregateRepository(
        impl: NutritionAggregateRepositoryImpl
    ): NutritionAggregateRepository
}