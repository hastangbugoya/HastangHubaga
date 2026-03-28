package com.example.hastanghubaga.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.usecase.meal.BuildMealsFromAkImportedLogsUseCase
import com.example.hastanghubaga.domain.usecase.meal.ImportAkLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val importAkLogsUseCase: ImportAkLogsUseCase,
    private val buildMealsFromAkImportedLogsUseCase: BuildMealsFromAkImportedLogsUseCase
) : ViewModel() {

    sealed interface UiEvent {
        data class ShowMessage(val message: String) : UiEvent
    }

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun onImportFromAdobongKangkongClick(force: Boolean = false) {
        viewModelScope.launch {
            when (val result = importAkLogsUseCase(force = force)) {
                is ImportAkLogsUseCase.Result.Success -> {
                    var derivedMealCount = 0

                    result.affectedDateIsos.forEach { dateIso ->
                        val derivedMeals = buildMealsFromAkImportedLogsUseCase(dateIso)
                        derivedMealCount += derivedMeals.size
                    }

                    val message = buildString {
                        append("Imported ")
                        append(result.importedCount)
                        append(" AdobongKangkong log")
                        if (result.importedCount != 1) append("s")
                        append(". ")

                        append(result.changedCount)
                        append(" row")
                        if (result.changedCount != 1) append("s")
                        append(" inserted or updated.")

                        if (result.affectedDateIsos.isNotEmpty()) {
                            append(" Built ")
                            append(derivedMealCount)
                            append(" derived HH meal")
                            if (derivedMealCount != 1) append("s")
                            append(" across ")
                            append(result.affectedDateIsos.size)
                            append(" affected date")
                            if (result.affectedDateIsos.size != 1) append("s")
                            append(".")
                        }
                    }

                    _events.emit(UiEvent.ShowMessage(message))
                }

                is ImportAkLogsUseCase.Result.Error -> {
                    _events.emit(
                        UiEvent.ShowMessage(
                            result.message.ifBlank { "AdobongKangkong import failed." }
                        )
                    )
                }
            }
        }
    }
}