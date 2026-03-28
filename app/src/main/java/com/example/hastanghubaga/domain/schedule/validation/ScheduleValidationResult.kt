package com.example.hastanghubaga.domain.schedule.validation

sealed interface ScheduleValidationResult {
    data object Valid : ScheduleValidationResult
    data class Invalid(
        val errors: List<ScheduleValidationError>
    ) : ScheduleValidationResult
}