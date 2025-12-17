package com.example.hastanghubaga.feature.today

sealed class TodayScreenEvent {
    data object Refresh : TodayScreenEvent()
}