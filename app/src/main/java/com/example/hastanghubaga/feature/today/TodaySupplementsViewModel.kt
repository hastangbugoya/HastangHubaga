package com.example.hastanghubaga.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.usecase.GetSupplementsForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodaySupplementsViewModel @Inject constructor(
    private val getSupplementsForDateUseCase: GetSupplementsForDateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TodaySupplementsState())
    val state: StateFlow<TodaySupplementsState> = _state

    init {
        loadToday()
    }

    fun onEvent(event: TodaySupplementsEvent) {
        when (event) {
            TodaySupplementsEvent.Refresh -> loadToday()
        }
    }

    private fun loadToday() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val today = LocalDate.now()
                val supplements = getSupplementsForDateUseCase(today)

                _state.update {
                    it.copy(
                        isLoading = false,
                        todaySupplements = supplements
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
}
