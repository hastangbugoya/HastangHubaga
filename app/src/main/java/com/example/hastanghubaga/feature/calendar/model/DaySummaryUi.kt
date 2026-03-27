package com.example.hastanghubaga.feature.calendar.model

import kotlinx.datetime.LocalDate

data class DaySummaryUi(
    val date: LocalDate,
    val supplementsLogged: Int,
    val mealsLogged: Int,
    val activitiesCompleted: Int,
    val hasImportedNutritionData: Boolean = false
)