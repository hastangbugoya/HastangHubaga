package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.MealRepositoryImpl
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class MealModule {

    @Binds
    abstract fun bindMealRepository(
        impl: MealRepositoryImpl
    ): MealRepository
}