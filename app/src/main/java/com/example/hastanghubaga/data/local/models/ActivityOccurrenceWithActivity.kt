package com.example.hastanghubaga.data.local.models

import androidx.room.Embedded
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity

/**
 * Room join projection combining a concrete planner occurrence with its
 * underlying activity template.
 *
 * Why this exists:
 * - timeline/planner placement must come from [occurrence]
 * - activity display metadata still comes from [activity]
 *
 * Canonical rule:
 * - scheduled day rendering should be driven by occurrence rows
 * - not by ActivityEntity.startTimestamp
 */
data class ActivityOccurrenceWithActivity(
    @Embedded(prefix = "occ_")
    val occurrence: ActivityOccurrenceEntity,

    @Embedded(prefix = "act_")
    val activity: ActivityEntity
)