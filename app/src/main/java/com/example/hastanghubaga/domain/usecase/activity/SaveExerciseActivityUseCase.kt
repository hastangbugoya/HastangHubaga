package com.example.hastanghubaga.domain.usecase.activity

import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.repository.activity.ActivityLogRepository
import javax.inject.Inject

/**
 * Saves one actual logged activity session.
 *
 * Canonical activity model:
 * - ActivityEntity = template
 * - ActivityOccurrenceEntity = planned occurrence
 * - ActivityLogEntity = actual performed session
 *
 * This use case writes only to the activity log layer.
 * It must NOT create or mutate template rows.
 *
 * Planned logging contract:
 * - if [occurrenceId] is non-null, it identifies one specific planned occurrence
 * - repeated logging for the same non-null [occurrenceId] must update/replace the
 *   existing persisted log row rather than create a duplicate
 * - if [occurrenceId] is null, this is treated as an ad-hoc / force-logged activity
 *   and may insert as a new independent row
 *
 * Snapshot contract:
 * - [title] is the user-facing display name and must be persisted into the log
 * - [type] remains category-only metadata
 * - history/timeline must not derive title from enum names
 */
class SaveExerciseActivityUseCase @Inject constructor(
    private val repo: ActivityLogRepository
) {
    suspend operator fun invoke(
        activityId: Long?,
        occurrenceId: String?,
        title: String,
        type: ActivityType,
        startTimestamp: Long,
        endTimestamp: Long,
        notes: String?,
        intensity: Int?,
        savedAddressId: Long?,
        addressAsRawString: String?,
        addressDisplayText: String?
    ): Long {
        return repo.insertActivityLog(
            activityId = activityId,
            occurrenceId = occurrenceId,
            title = title,
            activityType = type,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            notes = notes,
            intensity = intensity,
            savedAddressId = savedAddressId,
            addressAsRawString = addressAsRawString,
            addressDisplayText = addressDisplayText
        )
    }
}