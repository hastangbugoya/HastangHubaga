package com.example.hastanghubaga.domain.schedule.model

sealed interface ScheduleTiming {
    data class FixedTimes(
        val times: List<TimeOfDaySpec>
    ) : ScheduleTiming

    data class AnchoredTimes(
        val occurrences: List<AnchoredTimeSpec>
    ) : ScheduleTiming
}