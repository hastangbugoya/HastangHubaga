package com.example.hastanghubaga.feature.settings.eventtimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import javax.inject.Inject

data class EventTimeUi(
    val anchor: DoseAnchorType,
    val time: LocalTime
)

@HiltViewModel
class DefaultEventTimesViewModel @Inject constructor(
    private val eventTimeDao: EventTimeDao
) : ViewModel() {

    private val _state = MutableStateFlow<List<EventTimeUi>>(emptyList())
    val state: StateFlow<List<EventTimeUi>> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val defaults = eventTimeDao.getAllDefaults()

            _state.value = defaults.map {
                EventTimeUi(
                    anchor = it.anchor,
                    time = LocalTime.fromSecondOfDay(it.timeSeconds)
                )
            }.sortedBy { it.time }
        }
    }

    fun updateTime(anchor: DoseAnchorType, newTime: LocalTime) {
        viewModelScope.launch {
            eventTimeDao.upsertDefault(
                EventDefaultTimeEntity(
                    anchor = anchor,
                    timeSeconds = newTime.toSecondOfDay()
                )
            )
            load()
        }
    }
}