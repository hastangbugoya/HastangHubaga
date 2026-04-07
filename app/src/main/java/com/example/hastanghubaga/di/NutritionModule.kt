package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.NutrientTotalsRepositoryImpl
import com.example.hastanghubaga.data.repository.NutritionGoalsRepositoryImpl
import com.example.hastanghubaga.data.repository.NutritionPlanRepositoryImpl
import com.example.hastanghubaga.domain.repository.nutrition.NutrientTotalsRepository
import com.example.hastanghubaga.domain.repository.nutrition.NutritionGoalsRepository
import com.example.hastanghubaga.domain.repository.nutrition.NutritionPlanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NutritionModule {

    @Binds
    @Singleton
    abstract fun bindNutritionTotalsRepository(
        impl: NutrientTotalsRepositoryImpl
    ): NutrientTotalsRepository
}
