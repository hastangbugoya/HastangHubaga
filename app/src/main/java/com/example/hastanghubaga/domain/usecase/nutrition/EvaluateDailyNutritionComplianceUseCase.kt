package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanSuccessCriteriaDao
import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.domain.model.nutrition.DailyComplianceResult
import com.example.hastanghubaga.domain.model.nutrition.DailyNutritionIntake
import com.example.hastanghubaga.domain.model.nutrition.NutrientEvaluationResult

/**
 * Evaluates daily nutrition compliance for a single date.
 *
 * This is the top-level orchestration use case for HH daily nutrition compliance.
 *
 * Responsibilities:
 * - resolve active/effective plans for the date
 * - batch-load plan goals
 * - batch-load custom success criteria
 * - evaluate each plan using EvaluatePlanComplianceUseCase
 * - compute day-level success
 * - compute rolled-up day nutrient comparisons across active plans
 *
 * Why this exists:
 * - plan resolution should happen once, centrally
 * - plan success and day success should be determined by domain logic, not UI
 * - future calendar/month features should be able to reuse this same result shape
 *
 * ---
 * ## Day success rule
 *
 * Initial rule:
 * - no active/effective plans => successful day
 * - otherwise, the day is successful only if ALL applicable plans are successful
 *
 * This can later become configurable without changing lower-level evaluation.
 *
 * ---
 * ## Rolled-up day nutrient results
 *
 * In addition to per-plan results, this use case produces a day-level nutrient view
 * by combining active plan goal rows for the same nutrient key:
 *
 * - effective min = highest active min
 * - effective max = lowest active max
 * - target remains advisory for now
 *
 * This mirrors the design documented on the plan/goal entities and gives the app a
 * consistent daily detail view even when multiple plans are active.
 *
 * If future policy for conflicting ranges changes, update the helper methods here.
 *
 * ---
 * ## Future AI/dev notes
 *
 * DO NOT:
 * - bypass plan-level successMode logic
 * - resolve plans in UI
 * - duplicate effective-range aggregation elsewhere
 *
 * ALWAYS:
 * - use DAO date-effective plan lookup
 * - batch-load goals/criteria where possible
 * - keep this use case deterministic and easy to unit test
 */
class EvaluateDailyNutritionComplianceUseCase(
    private val nutritionPlanEntityDao: NutritionPlanEntityDao,
    private val nutrientGoalDao: NutrientGoalDao,
    private val nutritionPlanSuccessCriteriaDao: NutritionPlanSuccessCriteriaDao,
    private val evaluatePlanComplianceUseCase: EvaluatePlanComplianceUseCase,
    private val evaluateNutrientUseCase: EvaluateNutrientUseCase
) {

    suspend operator fun invoke(
        intake: DailyNutritionIntake
    ): DailyComplianceResult {

        val activePlans = nutritionPlanEntityDao.getActivePlansEffectiveOn(
            dateMillis = intake.date
        )

        if (activePlans.isEmpty()) {
            return DailyComplianceResult(
                date = intake.date,
                isSuccessful = true,
                planResults = emptyList(),
                allNutrientResults = emptyList()
            )
        }

        val planIds = activePlans.map { it.id }

        val allGoals = nutrientGoalDao.getGoalsForPlans(planIds)
        val allCriteria = nutritionPlanSuccessCriteriaDao.getForPlans(planIds)

        val goalsByPlanId: Map<Long, List<NutrientGoalEntity>> = allGoals.groupBy { it.planId }
        val criteriaKeysByPlanId: Map<Long, Set<String>> = allCriteria
            .groupBy { it.planId }
            .mapValues { entry ->
                entry.value.map { it.nutrientKey }.toSet()
            }

        val planResults = activePlans.map { plan ->
            evaluatePlanComplianceUseCase(
                plan = plan,
                goals = goalsByPlanId[plan.id].orEmpty(),
                successCriteriaKeys = criteriaKeysByPlanId[plan.id].orEmpty(),
                intake = intake
            )
        }

        val dayIsSuccessful = planResults.all { it.isSuccessful }

        val rolledUpNutrientResults = buildRolledUpNutrientResults(
            goals = allGoals,
            intake = intake
        )

        return DailyComplianceResult(
            date = intake.date,
            isSuccessful = dayIsSuccessful,
            planResults = planResults,
            allNutrientResults = rolledUpNutrientResults
        )
    }

    /**
     * Builds a day-level nutrient view across all active plans.
     *
     * Multiple active plans may define constraints for the same nutrient key.
     * We combine them into a single effective comparison row using:
     * - min = highest defined min
     * - max = lowest defined max
     * - target = advisory only
     *
     * For target, the initial rule is:
     * - if every defined target agrees exactly -> use that value
     * - otherwise -> null
     *
     * This keeps target informational and avoids inventing semantics too early.
     */
    private fun buildRolledUpNutrientResults(
        goals: List<NutrientGoalEntity>,
        intake: DailyNutritionIntake
    ): List<NutrientEvaluationResult> {

        return goals
            .groupBy { it.nutrientKey }
            .toSortedMap()
            .map { (nutrientKey, nutrientGoals) ->

                val effectiveMin = nutrientGoals
                    .mapNotNull { it.minValue }
                    .maxOrNull()

                val effectiveMax = nutrientGoals
                    .mapNotNull { it.maxValue }
                    .minOrNull()

                val effectiveTarget = resolveEffectiveTarget(nutrientGoals)

                evaluateNutrientUseCase(
                    nutrientKey = nutrientKey,
                    intake = intake.nutrients[nutrientKey],
                    min = effectiveMin,
                    target = effectiveTarget,
                    max = effectiveMax
                )
            }
    }

    /**
     * Resolves an advisory day-level target across multiple plan rows for the same nutrient.
     *
     * Initial conservative rule:
     * - no targets -> null
     * - one unique target -> that target
     * - multiple different targets -> null
     *
     * This avoids implying a merged target strategy before the product explicitly wants one.
     */
    private fun resolveEffectiveTarget(
        goals: List<NutrientGoalEntity>
    ): Double? {
        val uniqueTargets = goals
            .mapNotNull { it.targetValue }
            .distinct()

        return if (uniqueTargets.size == 1) {
            uniqueTargets.first()
        } else {
            null
        }
    }
}