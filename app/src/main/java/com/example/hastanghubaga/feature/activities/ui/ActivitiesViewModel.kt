package com.example.hastanghubaga.feature.activities.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.domain.model.activity.ActivityType
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

data class ActivitiesUiState(
    val items: List<ActivityListItemUi> = emptyList(),
    val editor: ActivityEditorUiState? = null
)

data class ActivityEditorUiState(
    val id: Long? = null,
    val type: ActivityType = ActivityType.OTHER,
    val notes: String = "",
    val intensity: String = "",
    val isNew: Boolean = true
)

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val activityEntityDao: ActivityEntityDao
) : ViewModel() {

    private val editorState = MutableStateFlow<ActivityEditorUiState?>(null)

    private val timeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
    private val zoneId = ZoneId.systemDefault()

    private val itemsFlow =
        activityEntityDao
            .observeAllActivities()
            .map { activities ->
                activities.map { activity ->
                    ActivityListItemUi(
                        id = activity.id,
                        typeLabel = activity.type.toDisplayLabel(),
                        notes = activity.notes,
                        intensityLabel = activity.intensity?.let { "Intensity: $it" },
                        startLabel = formatTimestamp(activity.startTimestamp)
                    )
                }
            }

    val state: StateFlow<ActivitiesUiState> =
        combine(
            itemsFlow,
            editorState
        ) { items, editor ->
            ActivitiesUiState(
                items = items,
                editor = editor
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ActivitiesUiState()
        )

    fun onAddClick() {
        editorState.value = ActivityEditorUiState(
            isNew = true
        )
    }

    fun onEditClick(id: Long) {
        viewModelScope.launch {
            val activity = activityEntityDao.getActivityById(id) ?: return@launch
            editorState.value = ActivityEditorUiState(
                id = activity.id,
                type = activity.type,
                notes = activity.notes.orEmpty(),
                intensity = activity.intensity?.toString().orEmpty(),
                isNew = false
            )
        }
    }

    fun onTypeChanged(value: ActivityType) {
        editorState.update { current -> current?.copy(type = value) }
    }

    fun onNotesChanged(value: String) {
        editorState.update { current -> current?.copy(notes = value) }
    }

    fun onIntensityChanged(value: String) {
        editorState.update { current -> current?.copy(intensity = value) }
    }

    fun onDismissEditor() {
        editorState.value = null
    }

    fun onSaveClick() {
        val editor = editorState.value ?: return

        viewModelScope.launch {
            val parsedIntensity = editor.intensity.trim().toIntOrNull()

            if (editor.isNew) {
                activityEntityDao.insertActivity(
                    ActivityEntity(
                        type = editor.type,
                        startTimestamp = System.currentTimeMillis(),
                        endTimestamp = null,
                        notes = editor.notes.trim().ifBlank { null },
                        intensity = parsedIntensity
                    )
                )
            } else {
                val existing = editor.id?.let { activityEntityDao.getActivityById(it) }
                    ?: return@launch

                activityEntityDao.updateActivity(
                    existing.copy(
                        type = editor.type,
                        notes = editor.notes.trim().ifBlank { null },
                        intensity = parsedIntensity
                    )
                )
            }

            editorState.value = null
        }
    }

    fun onDeleteClick() {
        val editor = editorState.value ?: return
        val id = editor.id ?: return

        viewModelScope.launch {
            val existing = activityEntityDao.getActivityById(id) ?: return@launch
            activityEntityDao.deleteActivity(existing)
            editorState.value = null
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(zoneId)
            .format(timeFormatter)
    }
}

private fun ActivityType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }