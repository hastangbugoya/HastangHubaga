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
import com.example.hastanghubaga.domain.usecase.nutrition.AkImportedGoalValue
import com.example.hastanghubaga.domain.usecase.nutrition.ReadAkNutritionGoalsUseCase
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

data class AkImportedGoalPickerItemUi(
    val selectionKey: String,
    val nutrientKey: String,
    val displayName: String,
    val unitLabel: String?,
    val minValue: Double?,
    val targetValue: Double?,
    val maxValue: Double?,
    val sourceKindLabel: String,
    val hhSupportText: String
)

data class AkImportedGoalPickerState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val exportedAtEpochMs: Long? = null,
    val loadedAtEpochMs: Long? = null,
    val source: String? = null,
    val items: List<AkImportedGoalPickerItemUi> = emptyList(),
    val selectedKeys: Set<String> = emptySet(),
    val errorMessage: String? = null
) {
    val hasCachedItems: Boolean = items.isNotEmpty()
}

data class NutritionGoalsUiState(
    val items: List<NutritionPlanListItemUi> = emptyList(),
    val nutrientCatalog: List<NutritionGoalCatalogItemUi> = emptyList(),
    val editor: NutritionPlanEditorState? = null,
    val akImportedGoalPicker: AkImportedGoalPickerState = AkImportedGoalPickerState()
)

@HiltViewModel
class NutritionGoalsViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val nutritionPlanEntityDao: NutritionPlanEntityDao,
    private val nutrientGoalDao: NutrientGoalDao,
    private val ingredientEntityDao: IngredientEntityDao,
    private val readAkNutritionGoalsUseCase: ReadAkNutritionGoalsUseCase
) : ViewModel() {

    private val editorState = MutableStateFlow<NutritionPlanEditorState?>(null)
    private val akImportedGoalPickerState = MutableStateFlow(AkImportedGoalPickerState())

    /**
     * Raw cached AK rows.
     *
     * We intentionally cache the imported AK values separately from the picker UI items
     * so the picker can be rebuilt later with fresh HH editor-side comparison/support text.
     */
    private var cachedAkImportedGoals: List<AkImportedGoalValue> = emptyList()

    val uiState: StateFlow<NutritionGoalsUiState> =
        combine(
            nutritionPlanEntityDao.observeAllPlans(),
            ingredientEntityDao.observeAllIngredients(),
            editorState,
            akImportedGoalPickerState
        ) { plans, ingredients, editor, akImportedGoalPicker ->
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
                editor = editor,
                akImportedGoalPicker = akImportedGoalPicker
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
        akImportedGoalPickerState.update { current ->
            current.copy(
                isVisible = false,
                selectedKeys = emptySet(),
                errorMessage = null
            )
        }
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

    /**
     * Manual, user-triggered AK reference flow.
     *
     * Behavior:
     * - only works while an editor is open
     * - reuses cached imported AK data when still fresh
     * - otherwise re-reads the current AK goals snapshot
     * - never persists imported AK data directly
     * - only exposes a temporary picker/reference buffer in VM state
     */
    fun onReferToAkClick() {
        val currentEditor = editorState.value ?: return

        val now = Clock.System.now().toEpochMilliseconds()
        val currentPickerState = akImportedGoalPickerState.value
        val hasFreshCache = currentPickerState.loadedAtEpochMs != null &&
                cachedAkImportedGoals.isNotEmpty() &&
                now - currentPickerState.loadedAtEpochMs <= AK_REFERENCE_STALE_AFTER_MS

        if (hasFreshCache) {
            akImportedGoalPickerState.update { current ->
                current.copy(
                    isVisible = true,
                    isLoading = false,
                    items = cachedAkImportedGoals.map { importedGoal ->
                        importedGoal.toPickerItemUi(currentEditor)
                    },
                    errorMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            akImportedGoalPickerState.update { current ->
                current.copy(
                    isVisible = true,
                    isLoading = true,
                    errorMessage = null,
                    selectedKeys = emptySet()
                )
            }

            when (val result = readAkNutritionGoalsUseCase()) {
                is ReadAkNutritionGoalsUseCase.Result.Success -> {
                    val loadedAt = Clock.System.now().toEpochMilliseconds()
                    val importedRows = result.snapshot.macros + result.snapshot.nutrients
                    cachedAkImportedGoals = importedRows

                    akImportedGoalPickerState.value = AkImportedGoalPickerState(
                        isVisible = true,
                        isLoading = false,
                        exportedAtEpochMs = result.snapshot.exportedAtEpochMs,
                        loadedAtEpochMs = loadedAt,
                        source = result.snapshot.source,
                        items = importedRows.map { importedGoal ->
                            importedGoal.toPickerItemUi(currentEditor)
                        },
                        selectedKeys = emptySet(),
                        errorMessage = null
                    )

                    editorState.update { current ->
                        current?.copy(errorMessage = null)
                    }
                }

                is ReadAkNutritionGoalsUseCase.Result.Error -> {
                    cachedAkImportedGoals = emptyList()

                    akImportedGoalPickerState.update { current ->
                        current.copy(
                            isVisible = false,
                            isLoading = false,
                            items = emptyList(),
                            selectedKeys = emptySet(),
                            errorMessage = result.message.ifBlank {
                                "Failed to read current AK goals."
                            }
                        )
                    }

                    editorState.update { current ->
                        current?.copy(
                            errorMessage = result.message.ifBlank {
                                "Failed to read current AK goals."
                            }
                        )
                    }
                }
            }
        }
    }

    fun onDismissAkGoalPicker() {
        akImportedGoalPickerState.update { current ->
            current.copy(
                isVisible = false,
                isLoading = false,
                selectedKeys = emptySet(),
                errorMessage = null
            )
        }
    }

    fun onAkGoalCheckedChanged(
        selectionKey: String,
        isChecked: Boolean
    ) {
        akImportedGoalPickerState.update { current ->
            val updatedSelection = current.selectedKeys.toMutableSet().apply {
                if (isChecked) add(selectionKey) else remove(selectionKey)
            }

            current.copy(
                selectedKeys = updatedSelection,
                errorMessage = null
            )
        }
    }

    /**
     * Copies user-selected imported AK goal rows into the current editor only.
     *
     * Rules:
     * - does NOT persist anything
     * - if an imported nutrientKey already exists in the editor, replace that row's
     *   min/target/max and display metadata
     * - otherwise append a new row
     * - if the editor only has the default empty row, that placeholder row is removed
     *   before imported rows are added
     */
    fun onApplySelectedAkGoals() {
        val currentEditor = editorState.value ?: return
        val currentPicker = akImportedGoalPickerState.value

        if (currentPicker.selectedKeys.isEmpty()) {
            editorState.update { current ->
                current?.copy(errorMessage = "Select at least one AK goal to apply.")
            }
            return
        }

        val selectedImportedItems = currentPicker.items
            .filter { it.selectionKey in currentPicker.selectedKeys }

        if (selectedImportedItems.isEmpty()) {
            editorState.update { current ->
                current?.copy(errorMessage = "Selected AK goals could not be resolved.")
            }
            return
        }

        val baseRows = currentEditor.goalRows
            .filterNot { it.isBlankPlaceholder() }
            .toMutableList()

        selectedImportedItems.forEach { importedItem ->
            val existingIndex = baseRows.indexOfFirst { row ->
                row.nutrientKey.trim() == importedItem.nutrientKey.trim()
            }

            val replacementRow = if (existingIndex >= 0) {
                baseRows[existingIndex].copy(
                    nutrientKey = importedItem.nutrientKey,
                    nutrientDisplayName = importedItem.displayName,
                    unitLabel = importedItem.unitLabel,
                    minValueInput = importedItem.minValue?.toString().orEmpty(),
                    targetValueInput = importedItem.targetValue?.toString().orEmpty(),
                    maxValueInput = importedItem.maxValue?.toString().orEmpty()
                )
            } else {
                NutritionGoalEditorRowUi(
                    rowId = nextNutritionGoalRowId(),
                    nutrientKey = importedItem.nutrientKey,
                    nutrientDisplayName = importedItem.displayName,
                    unitLabel = importedItem.unitLabel,
                    minValueInput = importedItem.minValue?.toString().orEmpty(),
                    targetValueInput = importedItem.targetValue?.toString().orEmpty(),
                    maxValueInput = importedItem.maxValue?.toString().orEmpty()
                )
            }

            if (existingIndex >= 0) {
                baseRows[existingIndex] = replacementRow
            } else {
                baseRows += replacementRow
            }
        }

        editorState.update { current ->
            current?.copy(
                goalRows = if (baseRows.isEmpty()) {
                    listOf(defaultNutritionGoalEditorRow())
                } else {
                    baseRows
                },
                errorMessage = null
            )
        }

        akImportedGoalPickerState.update { current ->
            current.copy(
                isVisible = false,
                selectedKeys = emptySet(),
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
                akImportedGoalPickerState.update { currentPicker ->
                    currentPicker.copy(
                        isVisible = false,
                        selectedKeys = emptySet(),
                        errorMessage = null
                    )
                }
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
            akImportedGoalPickerState.update { picker ->
                picker.copy(
                    isVisible = false,
                    selectedKeys = emptySet(),
                    errorMessage = null
                )
            }
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

    private fun AkImportedGoalValue.toPickerItemUi(
        currentEditor: NutritionPlanEditorState
    ): AkImportedGoalPickerItemUi {
        val ingredientCatalogMatch = uiState.value.nutrientCatalog
            .firstOrNull { it.nutrientKey == canonicalKey }

        val existingEditorRow = currentEditor.goalRows.firstOrNull { row ->
            row.nutrientKey.trim() == canonicalKey.trim()
        }

        return AkImportedGoalPickerItemUi(
            selectionKey = "${sourceKind.name}:$sourceKey",
            nutrientKey = canonicalKey,
            displayName = ingredientCatalogMatch?.displayName ?: displayName,
            unitLabel = ingredientCatalogMatch?.unitLabel ?: unit,
            minValue = minValue,
            targetValue = targetValue,
            maxValue = maxValue,
            sourceKindLabel = sourceKind.name,
            hhSupportText = buildHhSupportText(existingEditorRow)
        )
    }

    private fun buildHhSupportText(
        existingEditorRow: NutritionGoalEditorRowUi?
    ): String {
        if (existingEditorRow == null) {
            return "HH current: not set"
        }

        val parts = buildList {
            existingEditorRow.minValueInput.trim()
                .takeIf { it.isNotBlank() }
                ?.let { add("Min $it") }

            existingEditorRow.targetValueInput.trim()
                .takeIf { it.isNotBlank() }
                ?.let { add("Target $it") }

            existingEditorRow.maxValueInput.trim()
                .takeIf { it.isNotBlank() }
                ?.let { add("Max $it") }
        }

        return if (parts.isEmpty()) {
            "HH current: not set"
        } else {
            "HH current: ${parts.joinToString(" • ")}"
        }
    }
}

private data class ParsedGoalRow(
    val nutrientKey: String,
    val minValue: Double?,
    val targetValue: Double?,
    val maxValue: Double?
)

private const val SOURCE_TYPE_LOCAL = "LOCAL"
private const val AK_REFERENCE_STALE_AFTER_MS = 10 * 60 * 1000L

private fun defaultNutritionGoalEditorRow(): NutritionGoalEditorRowUi =
    NutritionGoalEditorRowUi(
        rowId = nextNutritionGoalRowId()
    )

private fun IngredientEntity.toNutritionKey(): String =
    code.trim().ifBlank { name.trim() }

private fun NutritionGoalEditorRowUi.isBlankPlaceholder(): Boolean {
    return nutrientKey.isBlank() &&
            nutrientDisplayName.isBlank() &&
            unitLabel.isNullOrBlank() &&
            minValueInput.isBlank() &&
            targetValueInput.isBlank() &&
            maxValueInput.isBlank()
}

private var nextNutritionGoalGeneratedRowId: Long = 0L

private fun nextNutritionGoalRowId(): String {
    nextNutritionGoalGeneratedRowId += 1L
    return "nutrition_goal_row_$nextNutritionGoalGeneratedRowId"
}