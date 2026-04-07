package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanSuccessCriteriaDao
import com.example.hastanghubaga.domain.repository.nutrition.NutritionAggregateRepository
import com.example.hastanghubaga.domain.usecase.nutrition.EvaluateDailyNutritionComplianceUseCase
import com.example.hastanghubaga.domain.usecase.nutrition.EvaluateNutrientUseCase
import com.example.hastanghubaga.domain.usecase.nutrition.EvaluatePlanComplianceUseCase
import com.example.hastanghubaga.domain.usecase.nutrition.GetDailyNutritionComplianceUseCase
import com.example.hastanghubaga.domain.usecase.nutrition.GetLocalDailyNutritionIntakeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides pure/domain use cases.
 *
 * This module intentionally stays separate from:
 * - DatabaseModule (database + DAO providers)
 * - RepositoryModule (interface -> implementation bindings)
 *
 * Why:
 * - keeps DI responsibilities clean
 * - avoids turning DatabaseModule into a grab-bag
 * - matches HH's current architecture style with minimal disruption
 *
 * Future AI/dev note:
 * Add new pure use cases here unless there is a strong reason to colocate them
 * elsewhere.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideEvaluateNutrientUseCase(): EvaluateNutrientUseCase =
        EvaluateNutrientUseCase()

    @Provides
    @Singleton
    fun provideEvaluatePlanComplianceUseCase(
        evaluateNutrientUseCase: EvaluateNutrientUseCase
    ): EvaluatePlanComplianceUseCase =
        EvaluatePlanComplianceUseCase(
            evaluateNutrientUseCase = evaluateNutrientUseCase
        )

    @Provides
    @Singleton
    fun provideEvaluateDailyNutritionComplianceUseCase(
        nutritionPlanEntityDao: NutritionPlanEntityDao,
        nutrientGoalDao: NutrientGoalDao,
        nutritionPlanSuccessCriteriaDao: NutritionPlanSuccessCriteriaDao,
        evaluatePlanComplianceUseCase: EvaluatePlanComplianceUseCase,
        evaluateNutrientUseCase: EvaluateNutrientUseCase
    ): EvaluateDailyNutritionComplianceUseCase =
        EvaluateDailyNutritionComplianceUseCase(
            nutritionPlanEntityDao = nutritionPlanEntityDao,
            nutrientGoalDao = nutrientGoalDao,
            nutritionPlanSuccessCriteriaDao = nutritionPlanSuccessCriteriaDao,
            evaluatePlanComplianceUseCase = evaluatePlanComplianceUseCase,
            evaluateNutrientUseCase = evaluateNutrientUseCase
        )

    @Provides
    @Singleton
    fun provideGetDailyNutritionComplianceUseCase(
        evaluateDailyNutritionComplianceUseCase: EvaluateDailyNutritionComplianceUseCase
    ): GetDailyNutritionComplianceUseCase =
        GetDailyNutritionComplianceUseCase(
            evaluateDailyNutritionComplianceUseCase = evaluateDailyNutritionComplianceUseCase
        )

    @Provides
    @Singleton
    fun provideGetLocalDailyNutritionIntakeUseCase(
        nutritionAggregateRepository: NutritionAggregateRepository
    ): GetLocalDailyNutritionIntakeUseCase =
        GetLocalDailyNutritionIntakeUseCase(
            nutritionAggregateRepository = nutritionAggregateRepository
        )
}