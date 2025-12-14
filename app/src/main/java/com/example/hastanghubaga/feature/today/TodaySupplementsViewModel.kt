package com.example.hastanghubaga.feature.today

import androidx.compose.material3.Text
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.ui.main.MainScreenIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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

    private var todayJob: Job? = null


    init {
        loadToday()
    }

    fun onEvent(event: TodaySupplementsEvent) {
        when (event) {
            TodaySupplementsEvent.Refresh -> loadToday()
        }
    }

    private fun loadToday(showSuccess: Boolean = false) {
        todayJob?.cancel()

        todayJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            emitMainScreenEvent(MainScreenIntent.SetLoading(true))

            try {
                val today = LocalDate.now()

                getSupplementsWithUserSettingsForDateUseCase(today)
                    .collectLatest { supplements ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                todaySupplements = supplements
                            )
                        }
                    }

                // NOTE: code below will NOT execute unless the flow completes
                // (which is expected behavior for Room flows)

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
        }

        // One-time UI effects that should happen per refresh
        viewModelScope.launch {
            if (showSuccess) {
                emitMainScreenEvent(
                    MainScreenIntent.ShowSnackbar("Today's supplements refreshed")
                )
            }

            emitMainScreenEvent(MainScreenIntent.ShowBanner("Today's supplements refreshed"))

            emitMainScreenEvent(
                MainScreenIntent.ShowBottomSheet {
                    Text("Loading finished")
                }
            )
        }
    }


    // Helper for screens to trigger UI events
    fun emitMainScreenEvent(intent: MainScreenIntent) {
        viewModelScope.launch {
            _events.emit(intent)
        }
    }
}
