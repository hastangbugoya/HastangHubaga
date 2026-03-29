package com.example.hastanghubaga.feature.meals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    val timeLabel: String,
    val notes: String?
)

data class MealEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val type: MealType = MealType.BREAKFAST,
    val treatAsAnchor: MealType? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isNew: Boolean = true
)

@HiltViewModel
class MealsViewModel @Inject constructor(
    private val mealEntityDao: MealEntityDao
) : ViewModel() {

    private val editorState = MutableStateFlow<MealEditorUiState?>(null)

    private val zoneId = ZoneId.systemDefault()
    private val listTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    private val itemsFlow =
        mealEntityDao
            .observeAllMeals()
            .map { joinedMeals ->
                joinedMeals.map { joined ->
                    val meal = joined.meal
                    MealListItemUi(
                        id = meal.id,
                        name = meal.name,
                        typeLabel = meal.type.toDisplayLabel(),
                        treatAsLabel = meal.treatAsAnchor?.let { "Treat as ${it.toDisplayLabel()}" },
                        timeLabel = formatListTimestamp(meal.timestamp),
                        notes = meal.notes
                    )
                }
            }

    val state: StateFlow<MealsUiState> =
        combine(
            itemsFlow,
            editorState
        ) { items, editor ->
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
            val meal = mealEntityDao.getMealByIdOnce(id) ?: return@launch

            editorState.value = MealEditorUiState(
                id = meal.id,
                name = meal.name,
                type = meal.type,
                treatAsAnchor = meal.treatAsAnchor,
                timestampMillis = meal.timestamp,
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

    fun onNotesChanged(value: String) {
        editorState.update { current -> current?.copy(notes = value) }
    }

    fun onTimestampChanged(value: Long) {
        editorState.update { current -> current?.copy(timestampMillis = value) }
    }

    fun onDismissEditor() {
        editorState.value = null
    }

    fun onSaveClick() {
        val editor = editorState.value ?: return

        viewModelScope.launch {
            val trimmedName = editor.name.trim()
            if (trimmedName.isBlank()) return@launch

            val entity = MealEntity(
                id = editor.id ?: 0L,
                name = trimmedName,
                type = editor.type,
                treatAsAnchor = editor.treatAsAnchor,
                timestamp = editor.timestampMillis,
                notes = editor.notes.trim().ifBlank { null }
            )

            mealEntityDao.upsertMeal(entity)
            editorState.value = null
        }
    }

    fun onDeleteClick() {
        val editor = editorState.value ?: return
        val id = editor.id ?: return

        viewModelScope.launch {
            mealEntityDao.deleteNutrition(id)
            mealEntityDao.deleteMealById(id)
            editorState.value = null
        }
    }

    private fun formatListTimestamp(timestampMillis: Long): String =
        Instant.ofEpochMilli(timestampMillis)
            .atZone(zoneId)
            .format(listTimeFormatter)
}

private fun MealType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }