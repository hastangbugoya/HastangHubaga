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
    val typeLabel: String,
    val timeLabel: String,
    val notes: String?
)

data class MealEditorUiState(
    val id: Long? = null,
    val type: MealType = MealType.BREAKFAST,
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
    private val editorTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    private val itemsFlow =
        mealEntityDao
            .observeAllMeals()
            .map { joinedMeals ->
                joinedMeals.map { joined ->
                    val meal = joined.meal
                    MealListItemUi(
                        id = meal.id,
                        typeLabel = meal.type.toDisplayLabel(),
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
                type = meal.type,
                timestampMillis = meal.timestamp,
                notes = meal.notes.orEmpty(),
                isNew = false
            )
        }
    }

    fun onTypeChanged(value: MealType) {
        editorState.update { current -> current?.copy(type = value) }
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
            val entity = MealEntity(
                id = editor.id ?: 0L,
                type = editor.type,
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

    fun getEditorTimeLabel(): String {
        val timestamp = editorState.value?.timestampMillis ?: System.currentTimeMillis()
        return formatEditorTimestamp(timestamp)
    }

    private fun formatListTimestamp(timestampMillis: Long): String =
        Instant.ofEpochMilli(timestampMillis)
            .atZone(zoneId)
            .format(listTimeFormatter)

    private fun formatEditorTimestamp(timestampMillis: Long): String =
        Instant.ofEpochMilli(timestampMillis)
            .atZone(zoneId)
            .format(editorTimeFormatter)
}

private fun MealType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }