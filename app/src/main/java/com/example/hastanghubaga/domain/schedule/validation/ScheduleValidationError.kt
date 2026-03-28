package com.example.hastanghubaga.domain.schedule.validation

enum class ScheduleValidationError {
    INVALID_DAILY_INTERVAL,
    INVALID_WEEKLY_INTERVAL,
    WEEKLY_DAYS_EMPTY,
    END_BEFORE_START,
    FIXED_TIMES_EMPTY,
    FIXED_TIMES_DUPLICATE,
    ANCHORED_TIMES_EMPTY
}