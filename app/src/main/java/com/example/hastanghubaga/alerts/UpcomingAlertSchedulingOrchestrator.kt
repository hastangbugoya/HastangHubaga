package com.example.hastanghubaga.alerts

import com.example.hastanghubaga.domain.usecase.alert.ObserveUpcomingScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpcomingAlertSchedulingOrchestrator @Inject constructor(
    private val observeUpcoming: ObserveUpcomingScheduleUseCase,
    private val scheduler: SupplementAlertScheduler
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            observeUpcoming()
                .distinctUntilChanged()
                .collect { scheduler.reschedule(it) }
        }
    }
}
