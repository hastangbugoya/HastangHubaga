package com.example.hastanghubaga.feature.today

sealed class TodaySupplementsEvent {
    data object Refresh : TodaySupplementsEvent()
}