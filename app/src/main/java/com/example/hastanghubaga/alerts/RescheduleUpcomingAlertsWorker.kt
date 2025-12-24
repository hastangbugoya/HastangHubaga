package com.example.hastanghubaga.alerts

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.hastanghubaga.domain.usecase.alert.ObserveUpcomingScheduleUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RescheduleUpcomingAlertsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val observeUpcoming: ObserveUpcomingScheduleUseCase,
    private val scheduler: SupplementAlertScheduler
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val items = observeUpcoming().first()
        scheduler.reschedule(items)
        return Result.success()
    }
}
