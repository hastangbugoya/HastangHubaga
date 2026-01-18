package com.example.hastanghubaga.domain.usecase.activity

import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import javax.inject.Inject

class SaveExerciseActivityUseCase @Inject constructor(
    private val repo: ActivityRepository
) {
    suspend operator fun invoke(
        type: ActivityType,
        startTimestamp: Long,
        endTimestamp: Long,
        notes: String?,
        intensity: Int?
    ): Long {
        return repo.insertActivity(
            type = type,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            notes = notes,
            intensity = intensity
        )
    }
}
