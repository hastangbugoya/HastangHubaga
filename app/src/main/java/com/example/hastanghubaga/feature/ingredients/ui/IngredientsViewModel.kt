package com.example.hastanghubaga.feature.ingredients.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IngredientEditorState(
    val ingredientId: Long? = null,
    val code: String = "",
    val name: String = "",
    val defaultUnit: IngredientUnit = IngredientUnit.MG,
    val rdaValue: String = "",
    val rdaUnit: IngredientUnit? = null,
    val upperLimitValue: String = "",
    val upperLimitUnit: IngredientUnit? = null,
    val category: String = "",
    val isVisible: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isEditing: Boolean = ingredientId != null
}

data class IngredientsUiState(
    val ingredients: List<IngredientEntity> = emptyList(),
    val editor: IngredientEditorState = IngredientEditorState()
)

@HiltViewModel
class IngredientsViewModel @Inject constructor(
    private val ingredientDao: IngredientEntityDao
) : ViewModel() {

    private val ingredientsFlow = ingredientDao.getAllIngredientsFlow()
    private val editorState = MutableStateFlow(IngredientEditorState())

    val uiState: StateFlow<IngredientsUiState> =
        combine(ingredientsFlow, editorState) { ingredients, editor ->
            IngredientsUiState(
                ingredients = ingredients,
                editor = editor
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IngredientsUiState()
        )

    fun onAddClick() {
        editorState.value = IngredientEditorState(
            defaultUnit = IngredientUnit.MG,
            isVisible = true
        )
    }

    fun onIngredientClick(ingredientId: Long) {
        viewModelScope.launch {
            val ingredient = ingredientDao.getIngredientById(ingredientId) ?: return@launch
            editorState.value = ingredient.toEditorState()
        }
    }

    fun onDismissEditor() {
        editorState.value = IngredientEditorState()
    }

    fun onCodeChanged(value: String) {
        editorState.update { it.copy(code = value, errorMessage = null) }
    }

    fun onNameChanged(value: String) {
        editorState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onDefaultUnitChanged(value: IngredientUnit) {
        editorState.update { it.copy(defaultUnit = value, errorMessage = null) }
    }

    fun onRdaValueChanged(value: String) {
        editorState.update { it.copy(rdaValue = value, errorMessage = null) }
    }

    fun onRdaUnitChanged(value: IngredientUnit?) {
        editorState.update { it.copy(rdaUnit = value, errorMessage = null) }
    }

    fun onUpperLimitValueChanged(value: String) {
        editorState.update { it.copy(upperLimitValue = value, errorMessage = null) }
    }

    fun onUpperLimitUnitChanged(value: IngredientUnit?) {
        editorState.update { it.copy(upperLimitUnit = value, errorMessage = null) }
    }

    fun onCategoryChanged(value: String) {
        editorState.update { it.copy(category = value, errorMessage = null) }
    }

    fun onSaveClick() {
        val current = editorState.value
        val trimmedName = current.name.trim()
        val trimmedCode = current.code.trim()
        val trimmedCategory = current.category.trim()

        if (trimmedName.isBlank()) {
            editorState.update { it.copy(errorMessage = "Name is required.") }
            return
        }

        val parsedRdaValue = current.rdaValue.toDoubleOrNull()
        if (current.rdaValue.isNotBlank() && parsedRdaValue == null) {
            editorState.update { it.copy(errorMessage = "RDA value must be a valid number.") }
            return
        }

        val parsedUpperLimitValue = current.upperLimitValue.toDoubleOrNull()
        if (current.upperLimitValue.isNotBlank() && parsedUpperLimitValue == null) {
            editorState.update { it.copy(errorMessage = "Upper limit value must be a valid number.") }
            return
        }

        viewModelScope.launch {
            editorState.update { it.copy(isSaving = true, errorMessage = null) }

            val existingByName = ingredientDao.getIngredientByName(trimmedName)
            if (existingByName != null && existingByName.id != current.ingredientId) {
                editorState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "An ingredient with that name already exists."
                    )
                }
                return@launch
            }

            val entity = IngredientEntity(
                id = current.ingredientId ?: 0L,
                code = trimmedCode,
                name = trimmedName,
                defaultUnit = current.defaultUnit,
                rdaValue = parsedRdaValue,
                rdaUnit = current.rdaUnit,
                upperLimitValue = parsedUpperLimitValue,
                upperLimitUnit = current.upperLimitUnit,
                category = trimmedCategory.ifBlank { null }
            )

            if (current.ingredientId == null) {
                ingredientDao.insertIngredient(entity)
            } else {
                ingredientDao.updateIngredient(entity)
            }

            editorState.value = IngredientEditorState()
        }
    }

    fun onDeleteClick() {
        val current = editorState.value
        val ingredientId = current.ingredientId ?: return

        viewModelScope.launch {
            val ingredient = ingredientDao.getIngredientById(ingredientId) ?: return@launch
            ingredientDao.deleteIngredient(ingredient)
            editorState.value = IngredientEditorState()
        }
    }

    private fun IngredientEntity.toEditorState(): IngredientEditorState {
        return IngredientEditorState(
            ingredientId = id,
            code = code,
            name = name,
            defaultUnit = defaultUnit,
            rdaValue = rdaValue?.toString().orEmpty(),
            rdaUnit = rdaUnit,
            upperLimitValue = upperLimitValue?.toString().orEmpty(),
            upperLimitUnit = upperLimitUnit,
            category = category.orEmpty(),
            isVisible = true,
            isSaving = false,
            errorMessage = null
        )
    }
}