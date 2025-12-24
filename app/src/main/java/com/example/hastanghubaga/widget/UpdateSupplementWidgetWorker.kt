package com.example.hastanghubaga.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock

@HiltWorker
class UpdateSupplementWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repo: SupplementRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = DomainTimePolicy.todayLocal(Clock.System)
            val active = repo.getActiveSupplements().first()
            val todaySupps = active.filter { repo.shouldTakeToday(it, today) }
            val names = todaySupps.map { it.name }

            SupplementWidgetPrefs.saveTodaySupplements(applicationContext, names)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
