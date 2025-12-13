package com.example.hastanghubaga.feature.today

import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.ui.main.MainScreenIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodaySupplementsViewModel @Inject constructor(
    private val getSupplementsWithUserSettingsForDateUseCase: GetSupplementsWithUserSettingsForDateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TodaySupplementsState())
    val state: StateFlow<TodaySupplementsState> = _state

    // MainScreenIntent event stream
    private val _events = MutableSharedFlow<MainScreenIntent>()
    val events: SharedFlow<MainScreenIntent> = _events

    init {
        loadToday()
    }

    fun onEvent(event: TodaySupplementsEvent) {
        when (event) {
            TodaySupplementsEvent.Refresh -> loadToday()
        }
    }

    private fun loadToday(showSuccess: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            emitMainScreenEvent(MainScreenIntent.SetLoading(true))

            try {
                val today = LocalDate.now()
                val supplements = getSupplementsWithUserSettingsForDateUseCase(today)

                getSupplementsWithUserSettingsForDateUseCase(today)
                    .collectLatest { supplements ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                todaySupplements = supplements
                            )
                        }
                    }

                emitMainScreenEvent(MainScreenIntent.SetLoading(false))

                if (showSuccess) {
                    emitMainScreenEvent(
                        MainScreenIntent.ShowSnackbar("Today's supplements refreshed")
                    )
                }

            } catch (e: Exception) {

                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }

                emitMainScreenEvent(MainScreenIntent.SetLoading(false))

                emitMainScreenEvent(
                    MainScreenIntent.ShowErrorSheet(
                        title = "Unable to load supplements",
                        message = e.message ?: "Unknown error"
                    )
                )
            }
            emitMainScreenEvent(MainScreenIntent.ShowBottomSheet(
                {
                    Text("Loading finished")
                }
            ))
            emitMainScreenEvent(MainScreenIntent.ShowBanner( "Today's supplements refreshed"))
        }
    }

    // Helper for screens to trigger UI events
    fun emitMainScreenEvent(intent: MainScreenIntent) {
        viewModelScope.launch {
            _events.emit(intent)
        }
    }
}
