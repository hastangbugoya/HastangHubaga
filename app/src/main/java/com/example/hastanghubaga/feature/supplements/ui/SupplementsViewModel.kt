package com.example.hastanghubaga.feature.supplements.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupplementsUiState(
    val items: List<SupplementListItemUi> = emptyList(),
    val editor: SupplementEditorUiState? = null
)

data class SupplementEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val brand: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val isNew: Boolean = true
)

@HiltViewModel
class SupplementsViewModel @Inject constructor(
    private val supplementEntityDao: SupplementEntityDao
) : ViewModel() {

    private val editorState = MutableStateFlow<SupplementEditorUiState?>(null)

    private val itemsFlow =
        supplementEntityDao
            .getAllSupplementsFlow()
            .map { supplements ->
                supplements.map { supplement ->
                    SupplementListItemUi(
                        id = supplement.id,
                        name = supplement.name,
                        brand = supplement.brand,
                        notes = supplement.notes,
                        isActive = supplement.isActive
                    )
                }
            }

    val state: StateFlow<SupplementsUiState> =
        combine(
            itemsFlow,
            editorState
        ) { items, editor ->
            SupplementsUiState(
                items = items,
                editor = editor
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SupplementsUiState()
        )

    fun onAddClick() {
        editorState.value = SupplementEditorUiState(
            isNew = true
        )
    }

    fun onEditClick(id: Long) {
        viewModelScope.launch {
            val supplement = supplementEntityDao.getSupplementById(id) ?: return@launch
            editorState.value = SupplementEditorUiState(
                id = supplement.id,
                name = supplement.name,
                brand = supplement.brand.orEmpty(),
                notes = supplement.notes.orEmpty(),
                isActive = supplement.isActive,
                isNew = false
            )
        }
    }

    fun onNameChanged(value: String) {
        editorState.update { current -> current?.copy(name = value) }
    }

    fun onBrandChanged(value: String) {
        editorState.update { current -> current?.copy(brand = value) }
    }

    fun onNotesChanged(value: String) {
        editorState.update { current -> current?.copy(notes = value) }
    }

    fun onIsActiveChanged(value: Boolean) {
        editorState.update { current -> current?.copy(isActive = value) }
    }

    fun onDismissEditor() {
        editorState.value = null
    }

    fun onSaveClick() {
        val editor = editorState.value ?: return
        val trimmedName = editor.name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            if (editor.isNew) {
                supplementEntityDao.insertSupplement(
                    SupplementEntity(
                        name = trimmedName,
                        brand = editor.brand.trim().ifBlank { null },
                        notes = editor.notes.trim().ifBlank { null },
                        recommendedServingSize = 1.0,
                        recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
                        servingsPerDay = 1.0,
                        doseAnchorType = DoseAnchorType.MIDNIGHT,
                        frequencyType = FrequencyType.DAILY,
                        isActive = editor.isActive
                    )
                )
            } else {
                val existing = editor.id?.let { supplementEntityDao.getSupplementById(it) }
                    ?: return@launch

                supplementEntityDao.updateSupplement(
                    existing.copy(
                        name = trimmedName,
                        brand = editor.brand.trim().ifBlank { null },
                        notes = editor.notes.trim().ifBlank { null },
                        isActive = editor.isActive
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
            val existing = supplementEntityDao.getSupplementById(id) ?: return@launch
            supplementEntityDao.deleteSupplement(existing)
            editorState.value = null
        }
    }
}