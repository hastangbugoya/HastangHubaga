package com.example.hastanghubaga.domain.model.activity

enum class ActivityType {
    WALKING,
    RUNNING,
    CYCLING,
    STRENGTH_TRAINING,
    YOGA,
    SWIMMING,
    SPORTS,
    WORK,
    COMMUTE,
    RELAX,
    SLEEP,
    MEAL,
    OTHER
}

val ActivityType.isExercise: Boolean
    get() = when (this) {
        ActivityType.WALKING,
        ActivityType.RUNNING,
        ActivityType.CYCLING,
        ActivityType.STRENGTH_TRAINING,
        ActivityType.YOGA,
        ActivityType.SWIMMING,
        ActivityType.SPORTS -> true
        else -> false
    }