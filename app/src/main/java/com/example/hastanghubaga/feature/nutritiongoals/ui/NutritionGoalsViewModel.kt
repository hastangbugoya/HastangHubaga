package com.example.hastanghubaga.feature.nutritiongoals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class NutritionGoalCatalogItemUi(
    val nutrientKey: String,
    val displayName: String,
    val unitLabel: String?
)

data class NutritionPlanListItemUi(
    val id: Long,
    val name: String,
    val typeLabel: String,
    val isActive: Boolean,
    val startDate: Long,
    val endDate: Long?,
    val sourceType: String
)

data class NutritionGoalEditorRowUi(
    val rowId: String,
    val nutrientKey: String = "",
    val nutrientDisplayName: String = "",
    val unitLabel: String? = null,
    val minValueInput: String = "",
    val targetValueInput: String = "",
    val maxValueInput: String = ""
)

data class NutritionPlanEditorState(
    val planId: Long? = null,
    val name: String = "",
    val type: NutritionGoalType = NutritionGoalType.CUSTOM,
    val startDateInput: String = "",
    val endDateInput: String = "",
    val isActive: Boolean = true,
    val sourceType: String = SOURCE_TYPE_LOCAL,
    val sourcePlanId: String? = null,
    val createdAt: Long? = null,
    val goalRows: List<NutritionGoalEditorRowUi> = listOf(defaultNutritionGoalEditorRow()),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isEditing: Boolean = planId != null
}

data class NutritionGoalsUiState(
    val items: List<NutritionPlanListItemUi> = emptyList(),
    val nutrientCatalog: List<NutritionGoalCatalogItemUi> = emptyList(),
    val editor: NutritionPlanEditorState? = null
)

@HiltViewModel
class NutritionGoalsViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val nutritionPlanEntityDao: NutritionPlanEntityDao,
    private val nutrientGoalDao: NutrientGoalDao,
    private val ingredientEntityDao: IngredientEntityDao
) : ViewModel() {

    private val editorState = MutableStateFlow<NutritionPlanEditorState?>(null)

    val uiState: StateFlow<NutritionGoalsUiState> =
        combine(
            nutritionPlanEntityDao.observeAllPlans(),
            ingredientEntityDao.observeAllIngredients(),
            editorState
        ) { plans, ingredients, editor ->
            NutritionGoalsUiState(
                items = plans.map { plan ->
                    NutritionPlanListItemUi(
                        id = plan.id,
                        name = plan.name,
                        typeLabel = plan.type.displayName,
                        isActive = plan.isActive,
                        startDate = plan.startDate,
                        endDate = plan.endDate,
                        sourceType = plan.sourceType
                    )
                },
                nutrientCatalog = ingredients
                    .map { ingredient ->
                        NutritionGoalCatalogItemUi(
                            nutrientKey = ingredient.toNutritionKey(),
                            displayName = ingredient.name,
                            unitLabel = ingredient.defaultUnit.name
                        )
                    }
                    .sortedBy { it.displayName },
                editor = editor
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NutritionGoalsUiState()
        )

    fun onAddClick() {
        val now = Clock.System.now().toEpochMilliseconds()
        editorState.value = NutritionPlanEditorState(
            type = NutritionGoalType.CUSTOM,
            startDateInput = now.toString(),
            isActive = true,
            sourceType = SOURCE_TYPE_LOCAL,
            sourcePlanId = null,
            createdAt = null,
            goalRows = listOf(defaultNutritionGoalEditorRow())
        )
    }

    fun onPlanClick(planId: Long) {
        viewModelScope.launch {
            val plan = nutritionPlanEntityDao.getPlanById(planId) ?: return@launch
            val goals = nutrientGoalDao.getGoalsForPlan(planId)
            val ingredientsByKey = ingredientEntityDao
                .getAllIngredients()
                .associateBy { it.toNutritionKey() }

            editorState.value = NutritionPlanEditorState(
                planId = plan.id,
                name = plan.name,
                type = plan.type,
                startDateInput = plan.startDate.toString(),
                endDateInput = plan.endDate?.toString().orEmpty(),
                isActive = plan.isActive,
                sourceType = plan.sourceType,
                sourcePlanId = plan.sourcePlanId,
                createdAt = plan.createdAt,
                goalRows = if (goals.isEmpty()) {
                    listOf(defaultNutritionGoalEditorRow())
                } else {
                    goals.map { goal ->
                        val ingredient = ingredientsByKey[goal.nutrientKey]
                        NutritionGoalEditorRowUi(
                            rowId = nextNutritionGoalRowId(),
                            nutrientKey = goal.nutrientKey,
                            nutrientDisplayName = ingredient?.name ?: goal.nutrientKey,
                            unitLabel = ingredient?.defaultUnit?.name,
                            minValueInput = goal.minValue?.toString().orEmpty(),
                            targetValueInput = goal.targetValue?.toString().orEmpty(),
                            maxValueInput = goal.maxValue?.toString().orEmpty()
                        )
                    }
                }
            )
        }
    }

    fun onDismissEditor() {
        editorState.value = null
    }

    fun onNameChanged(value: String) {
        editorState.update { current ->
            current?.copy(name = value, errorMessage = null)
        }
    }

    fun onTypeChanged(value: NutritionGoalType) {
        editorState.update { current ->
            current?.copy(type = value, errorMessage = null)
        }
    }

    fun onStartDateChanged(value: String) {
        editorState.update { current ->
            current?.copy(startDateInput = value, errorMessage = null)
        }
    }

    fun onEndDateChanged(value: String) {
        editorState.update { current ->
            current?.copy(endDateInput = value, errorMessage = null)
        }
    }

    fun onIsActiveChanged(value: Boolean) {
        editorState.update { current ->
            current?.copy(isActive = value, errorMessage = null)
        }
    }

    fun onAddGoalRow() {
        editorState.update { current ->
            current?.copy(
                goalRows = current.goalRows + defaultNutritionGoalEditorRow(),
                errorMessage = null
            )
        }
    }

    fun onRemoveGoalRow(rowId: String) {
        editorState.update { current ->
            current?.let {
                val remaining = it.goalRows.filterNot { row -> row.rowId == rowId }
                it.copy(
                    goalRows = if (remaining.isEmpty()) {
                        listOf(defaultNutritionGoalEditorRow())
                    } else {
                        remaining
                    },
                    errorMessage = null
                )
            }
        }
    }

    fun onGoalNutrientChanged(rowId: String, nutrientKey: String) {
        val selectedIngredient = uiState.value.nutrientCatalog
            .firstOrNull { it.nutrientKey == nutrientKey }

        editorState.update { current ->
            current?.copy(
                goalRows = current.goalRows.map { row ->
                    if (row.rowId == rowId) {
                        row.copy(
                            nutrientKey = nutrientKey,
                            nutrientDisplayName = selectedIngredient?.displayName ?: nutrientKey,
                            unitLabel = selectedIngredient?.unitLabel
                        )
                    } else {
                        row
                    }
                },
                errorMessage = null
            )
        }
    }

    fun onGoalMinChanged(rowId: String, value: String) {
        editorState.update { current ->
            current?.copy(
                goalRows = current.goalRows.map { row ->
                    if (row.rowId == rowId) row.copy(minValueInput = value) else row
                },
                errorMessage = null
            )
        }
    }

    fun onGoalTargetChanged(rowId: String, value: String) {
        editorState.update { current ->
            current?.copy(
                goalRows = current.goalRows.map { row ->
                    if (row.rowId == rowId) row.copy(targetValueInput = value) else row
                },
                errorMessage = null
            )
        }
    }

    fun onGoalMaxChanged(rowId: String, value: String) {
        editorState.update { current ->
            current?.copy(
                goalRows = current.goalRows.map { row ->
                    if (row.rowId == rowId) row.copy(maxValueInput = value) else row
                },
                errorMessage = null
            )
        }
    }

    fun onSaveClick() {
        val current = editorState.value ?: return
        val trimmedName = current.name.trim()

        if (trimmedName.isBlank()) {
            editorState.update { it?.copy(errorMessage = "Plan name is required.") }
            return
        }

        val startDate = current.startDateInput.trim().toLongOrNull()
        if (startDate == null) {
            editorState.update { it?.copy(errorMessage = "Start date must be a valid epoch millis value.") }
            return
        }

        val endDateInput = current.endDateInput.trim()
        val endDate = if (endDateInput.isBlank()) null else endDateInput.toLongOrNull()
        if (endDateInput.isNotBlank() && endDate == null) {
            editorState.update { it?.copy(errorMessage = "End date must be a valid epoch millis value.") }
            return
        }

        if (endDate != null && endDate < startDate) {
            editorState.update { it?.copy(errorMessage = "End date cannot be earlier than start date.") }
            return
        }

        val parsedGoalRows = current.goalRows.mapNotNull { row ->
            parseGoalRow(row)
        }

        if (parsedGoalRows.isEmpty()) {
            editorState.update {
                it?.copy(errorMessage = "Add at least one nutrient goal row with a nutrient and at least one value.")
            }
            return
        }

        val duplicateKeys = parsedGoalRows
            .groupBy { it.nutrientKey }
            .filterValues { it.size > 1 }
            .keys

        if (duplicateKeys.isNotEmpty()) {
            editorState.update {
                it?.copy(errorMessage = "Each nutrient can only appear once per plan.")
            }
            return
        }

        val invalidRow = current.goalRows.firstOrNull { row ->
            val hasAnyText = row.nutrientKey.isNotBlank() ||
                    row.minValueInput.isNotBlank() ||
                    row.targetValueInput.isNotBlank() ||
                    row.maxValueInput.isNotBlank()

            hasAnyText && parseGoalRow(row) == null
        }

        if (invalidRow != null) {
            editorState.update {
                it?.copy(errorMessage = "Each goal row must have a nutrient and at least one valid numeric min, target, or max value.")
            }
            return
        }

        viewModelScope.launch {
            editorState.update { it?.copy(isSaving = true, errorMessage = null) }

            val now = Clock.System.now().toEpochMilliseconds()
            val existingCreatedAt = current.createdAt ?: now

            try {
                appDatabase.withTransaction {
                    val planId = if (current.planId == null) {
                        nutritionPlanEntityDao.insert(
                            UserNutritionPlanEntity(
                                type = current.type,
                                name = trimmedName,
                                startDate = startDate,
                                endDate = endDate,
                                isActive = current.isActive,
                                sourceType = SOURCE_TYPE_LOCAL,
                                sourcePlanId = null,
                                createdAt = existingCreatedAt,
                                updatedAt = now
                            )
                        )
                    } else {
                        nutritionPlanEntityDao.update(
                            UserNutritionPlanEntity(
                                id = current.planId,
                                type = current.type,
                                name = trimmedName,
                                startDate = startDate,
                                endDate = endDate,
                                isActive = current.isActive,
                                sourceType = current.sourceType,
                                sourcePlanId = current.sourcePlanId,
                                createdAt = existingCreatedAt,
                                updatedAt = now
                            )
                        )
                        current.planId
                    }

                    nutrientGoalDao.deleteGoalsForPlan(planId)

                    nutrientGoalDao.insertAll(
                        parsedGoalRows.map { row ->
                            NutrientGoalEntity(
                                planId = planId,
                                nutrientKey = row.nutrientKey,
                                minValue = row.minValue,
                                targetValue = row.targetValue,
                                maxValue = row.maxValue
                            )
                        }
                    )
                }

                editorState.value = null
            } catch (t: Throwable) {
                editorState.update {
                    it?.copy(
                        isSaving = false,
                        errorMessage = t.message ?: "Failed to save nutrition plan."
                    )
                }
            }
        }
    }

    fun onDeleteClick() {
        val current = editorState.value ?: return
        val planId = current.planId ?: return

        viewModelScope.launch {
            val existing = nutritionPlanEntityDao.getPlanById(planId) ?: return@launch
            nutritionPlanEntityDao.delete(existing)
            editorState.value = null
        }
    }

    fun onTogglePlanActive(planId: Long, isActive: Boolean) {
        viewModelScope.launch {
            nutritionPlanEntityDao.setPlanActiveState(
                planId = planId,
                isActive = isActive,
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )

            editorState.update { current ->
                if (current?.planId == planId) {
                    current.copy(isActive = isActive)
                } else {
                    current
                }
            }
        }
    }

    private fun parseGoalRow(row: NutritionGoalEditorRowUi): ParsedGoalRow? {
        val nutrientKey = row.nutrientKey.trim()
        val minValue = row.minValueInput.trim().toDoubleOrNull()
        val targetValue = row.targetValueInput.trim().toDoubleOrNull()
        val maxValue = row.maxValueInput.trim().toDoubleOrNull()

        val hasAnyValueText = row.minValueInput.isNotBlank() ||
                row.targetValueInput.isNotBlank() ||
                row.maxValueInput.isNotBlank()

        val hasAnyParsedValue = minValue != null || targetValue != null || maxValue != null

        if (nutrientKey.isBlank() && !hasAnyValueText) {
            return null
        }

        if (nutrientKey.isBlank()) {
            return null
        }

        if (!hasAnyParsedValue) {
            return null
        }

        return ParsedGoalRow(
            nutrientKey = nutrientKey,
            minValue = minValue,
            targetValue = targetValue,
            maxValue = maxValue
        )
    }
}

private data class ParsedGoalRow(
    val nutrientKey: String,
    val minValue: Double?,
    val targetValue: Double?,
    val maxValue: Double?
)

private const val SOURCE_TYPE_LOCAL = "LOCAL"

private fun defaultNutritionGoalEditorRow(): NutritionGoalEditorRowUi =
    NutritionGoalEditorRowUi(
        rowId = nextNutritionGoalRowId()
    )

private fun IngredientEntity.toNutritionKey(): String =
    code.trim().ifBlank { name.trim() }

private var nextNutritionGoalGeneratedRowId: Long = 0L

private fun nextNutritionGoalRowId(): String {
    nextNutritionGoalGeneratedRowId += 1L
    return "nutrition_goal_row_$nextNutritionGoalGeneratedRowId"
}