package com.example.hastanghubaga.feature.meals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MealsUiState(
    val items: List<MealListItemUi> = emptyList(),
    val editor: MealEditorUiState? = null
)

data class MealListItemUi(
    val id: Long,
    val name: String,
    val typeLabel: String,
    val treatAsLabel: String?,
    val isActive: Boolean,
    val hasSchedule: Boolean,
    val notes: String?
)

data class MealEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val type: MealType = MealType.BREAKFAST,
    val treatAsAnchor: MealType? = null,
    val isActive: Boolean = true,
    val notes: String = "",
    val isNew: Boolean = true
)

@HiltViewModel
class MealsViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val editorState = MutableStateFlow<MealEditorUiState?>(null)

    val state: StateFlow<MealsUiState> =
        combine(
            mealRepository.observeAll(),
            editorState
        ) { meals, editor ->
            val items = meals.map { meal ->
                MealListItemUi(
                    id = meal.id,
                    name = meal.name,
                    typeLabel = meal.type.toDisplayLabel(),
                    treatAsLabel = meal.treatAsAnchor?.let { "Treat as ${it.toDisplayLabel()}" },
                    isActive = meal.isActive,
                    hasSchedule = false, // will be populated by the screen/UI layer once schedule editing wiring is added
                    notes = meal.notes
                )
            }

            MealsUiState(
                items = items,
                editor = editor
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MealsUiState()
        )

    fun onAddClick() {
        editorState.value = MealEditorUiState(
            isNew = true
        )
    }

    fun onEditClick(id: Long) {
        viewModelScope.launch {
            val meal = mealRepository.getMealById(id) ?: return@launch

            editorState.value = MealEditorUiState(
                id = meal.id,
                name = meal.name,
                type = meal.type,
                treatAsAnchor = meal.treatAsAnchor,
                isActive = meal.isActive,
                notes = meal.notes.orEmpty(),
                isNew = false
            )
        }
    }

    fun onNameChanged(value: String) {
        editorState.update { current -> current?.copy(name = value) }
    }

    fun onTypeChanged(value: MealType) {
        editorState.update { current -> current?.copy(type = value) }
    }

    fun onTreatAsAnchorChanged(value: MealType?) {
        editorState.update { current -> current?.copy(treatAsAnchor = value) }
    }

    fun onIsActiveChanged(value: Boolean) {
        editorState.update { current -> current?.copy(isActive = value) }
    }

    fun onNotesChanged(value: String) {
        editorState.update { current -> current?.copy(notes = value) }
    }

    fun onDismissEditor() {
        editorState.value = null
    }

    fun onSaveClick() {
        val editor = editorState.value ?: return

        viewModelScope.launch {
            val trimmedName = editor.name.trim()
            if (trimmedName.isBlank()) return@launch

            val mealId = editor.id ?: 0L

            mealRepository.upsertMeal(
                meal = MealEntity(
                    id = mealId,
                    name = trimmedName,
                    type = editor.type,
                    treatAsAnchor = editor.treatAsAnchor,
                    isActive = editor.isActive
                ),
                nutrition = MealNutritionEntity(
                    mealId = mealId,
                    calories = 0,
                    protein = 0.0,
                    carbs = 0.0,
                    fat = 0.0
                )
            )

            editorState.value = null
        }
    }

    fun onDeleteClick() {
        val editor = editorState.value ?: return
        val id = editor.id ?: return

        viewModelScope.launch {
            mealRepository.deleteMealById(id)
            editorState.value = null
        }
    }
}

private fun MealType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }