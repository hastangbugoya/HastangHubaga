package com.example.hastanghubaga.domain.model.calendar

data class DaySummaryUi(
    val date: kotlinx.datetime.LocalDate,
    val supplementsLogged: Int,
    val mealsLogged: Int,
    val activitiesCompleted: Int
)