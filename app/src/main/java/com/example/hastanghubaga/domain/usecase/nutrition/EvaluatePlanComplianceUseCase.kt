package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity
import com.example.hastanghubaga.domain.model.nutrition.DailyNutritionIntake
import com.example.hastanghubaga.domain.model.nutrition.NutrientEvaluationResult
import com.example.hastanghubaga.domain.model.nutrition.PlanComplianceResult
import com.example.hastanghubaga.data.local.entity.user.SUCCESS_MODE_ALL
import com.example.hastanghubaga.data.local.entity.user.SUCCESS_MODE_ANY
import com.example.hastanghubaga.data.local.entity.user.SUCCESS_MODE_CUSTOM
import com.example.hastanghubaga.data.local.entity.user.SUCCESS_MODE_NONE

/**
 * Evaluates a SINGLE nutrition plan against a day's intake.
 *
 * This is where successMode becomes REAL behavior.
 *
 * Inputs:
 * - plan
 * - its nutrient goals
 * - optional success criteria keys
 * - daily intake
 *
 * Output:
 * - PlanComplianceResult
 *
 * ---
 * ## Responsibilities
 *
 * 1. Evaluate all nutrient goals using EvaluateNutrientUseCase
 * 2. Determine which nutrients count toward success
 * 3. Apply successMode rules
 * 4. Produce a structured result for UI + higher-level aggregation
 *
 * ---
 * ## Important Design Rules
 *
 * - Goal rows define WHAT is tracked
 * - Success criteria define WHAT counts toward success
 * - successMode defines HOW success is evaluated
 *
 * ---
 * ## Future AI/dev notes
 *
 * DO NOT:
 * - evaluate success outside this use case
 * - assume all nutrients count toward success
 *
 * ALWAYS:
 * - respect successMode
 * - treat CUSTOM with empty criteria as success
 */
class EvaluatePlanComplianceUseCase(
    private val evaluateNutrientUseCase: EvaluateNutrientUseCase
) {

    operator fun invoke(
        plan: UserNutritionPlanEntity,
        goals: List<NutrientGoalEntity>,
        successCriteriaKeys: Set<String>, // empty if none or not CUSTOM
        intake: DailyNutritionIntake
    ): PlanComplianceResult {

        // -----------------------------------------------------
        // 1. Evaluate ALL nutrient goals
        // -----------------------------------------------------

        val nutrientResults = goals.map { goal ->

            val intakeValue = intake.nutrients[goal.nutrientKey]

            evaluateNutrientUseCase(
                nutrientKey = goal.nutrientKey,
                intake = intakeValue,
                min = goal.minValue,
                target = goal.targetValue,
                max = goal.maxValue
            )
        }

        // -----------------------------------------------------
        // 2. Determine evaluated nutrient keys (for success)
        // -----------------------------------------------------

        val evaluatedKeys: Set<String> = when (plan.successMode) {

            SUCCESS_MODE_CUSTOM -> successCriteriaKeys

            SUCCESS_MODE_NONE -> emptySet()

            else -> goals.map { it.nutrientKey }.toSet()
        }

        val evaluatedResults = nutrientResults.filter {
            it.nutrientKey in evaluatedKeys
        }

        // -----------------------------------------------------
        // 3. Determine success
        // -----------------------------------------------------

        val isSuccessful = when (plan.successMode) {

            SUCCESS_MODE_NONE -> {
                true
            }

            SUCCESS_MODE_CUSTOM -> {
                // If no selected nutrients → intentional success
                if (evaluatedResults.isEmpty()) {
                    true
                } else {
                    evaluatedResults.all { it.isWithinRange }
                }
            }

            SUCCESS_MODE_ALL -> {
                if (evaluatedResults.isEmpty()) {
                    true
                } else {
                    evaluatedResults.all { it.isWithinRange }
                }
            }

            SUCCESS_MODE_ANY -> {
                evaluatedResults.any { it.isWithinRange }
            }

            else -> {
                // Safe fallback
                evaluatedResults.all { it.isWithinRange }
            }
        }

        // -----------------------------------------------------
        // 4. Build result
        // -----------------------------------------------------

        return PlanComplianceResult(
            planId = plan.id,
            planName = plan.name,
            successMode = plan.successMode,
            isSuccessful = isSuccessful,
            evaluatedNutrientKeys = evaluatedKeys,
            allNutrientResults = nutrientResults
        )
    }
}