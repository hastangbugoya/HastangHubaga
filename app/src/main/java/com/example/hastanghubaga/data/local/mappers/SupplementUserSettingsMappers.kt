package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.domain.model.settings.SupplementSettings

fun SupplementUserSettingsEntity.toDomain(): SupplementSettings =
    SupplementSettings(
        supplementId = supplementId,
        preferredServingSize = preferredServingSize,
        preferredUnit = preferredUnit,
        preferredServingPerDay = preferredServingPerDay,
        isEnabled = isEnabled,
        userNotes = userNotes
    )