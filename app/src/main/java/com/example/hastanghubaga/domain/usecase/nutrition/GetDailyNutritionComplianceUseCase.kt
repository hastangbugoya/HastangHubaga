package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.nutrition.DailyComplianceResult
import com.example.hastanghubaga.domain.model.nutrition.DailyNutritionIntake

/**
 * Entry-point use case for fetching daily nutrition compliance.
 *
 * This acts as a thin orchestration layer between:
 * - data source (currently stub / later AK snapshot)
 * - compliance engine
 *
 * Why this exists:
 * - keeps EvaluateDailyNutritionComplianceUseCase pure and reusable
 * - allows swapping intake sources without touching compliance logic
 * - gives ViewModel / Timeline a clean API
 *
 * ---
 * ## Current behavior (Phase 1)
 *
 * Intake is passed in directly.
 *
 * ---
 * ## Future behavior (Phase 2+)
 *
 * This use case will:
 * - fetch AK daily snapshot via ContentProvider
 * - map snapshot → DailyNutritionIntake
 * - call compliance engine
 *
 * ---
 * ## Future AI/dev note
 *
 * DO NOT:
 * - couple EvaluateDailyNutritionComplianceUseCase to AK directly
 *
 * ALWAYS:
 * - keep intake sourcing separate from compliance evaluation
 */
class GetDailyNutritionComplianceUseCase(
    private val evaluateDailyNutritionComplianceUseCase: EvaluateDailyNutritionComplianceUseCase
) {

    suspend operator fun invoke(
        intake: DailyNutritionIntake
    ): DailyComplianceResult {
        return evaluateDailyNutritionComplianceUseCase(intake)
    }
}