package com.example.hastanghubaga.widget.di

import com.example.hastanghubaga.widget.calculator.DefaultNutritionProgressCalculator
import com.example.hastanghubaga.widget.calculator.NutritionProgressCalculator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetCalculatorModule {

    @Binds
    @Singleton
    abstract fun bindNutritionProgressCalculator(
        impl: DefaultNutritionProgressCalculator
    ): NutritionProgressCalculator
}
