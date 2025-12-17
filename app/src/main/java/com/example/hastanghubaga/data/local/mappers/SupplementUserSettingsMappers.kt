package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.domain.model.settings.SupplementSettings
import com.example.hastanghubaga.domain.model.supplement.UserSupplementSettings

fun SupplementUserSettingsEntity.toDomain(): SupplementSettings =
    SupplementSettings(
        supplementId = supplementId,
        preferredServingSize = preferredServingSize,
        preferredUnit = preferredUnit,
        preferredServingPerDay = preferredServingPerDay,
        isEnabled = isEnabled,
        userNotes = userNotes
    )

fun SupplementUserSettingsEntity.toUserSupplementSettings(): UserSupplementSettings? =
    UserSupplementSettings(
        preferredServingSize = preferredServingSize,
        preferredUnit = preferredUnit,
        preferredServingsPerDay = preferredServingPerDay,
        isEnabled = isEnabled,
        notes = userNotes,
    )


fun SupplementSettings.toEntity(): SupplementUserSettingsEntity =
    SupplementUserSettingsEntity(
        supplementId = supplementId,
        preferredServingSize = preferredServingSize,
        preferredUnit = preferredUnit,
        preferredServingPerDay = preferredServingPerDay,
        isEnabled = isEnabled,
        userNotes = userNotes
    )

fun SupplementSettings.toDomain(): UserSupplementSettings =
    UserSupplementSettings(
        preferredServingSize = preferredServingSize,
        preferredUnit = preferredUnit,
        preferredServingsPerDay = preferredServingPerDay,
        isEnabled = isEnabled,
        notes = userNotes,
    )

fun UserSupplementSettings.toEntity(supplementId: Long): SupplementUserSettingsEntity =
    SupplementUserSettingsEntity(
        supplementId = supplementId,
        preferredServingSize = preferredServingSize,
        preferredUnit = preferredUnit,
        preferredServingPerDay = preferredServingsPerDay,
        isEnabled = isEnabled,
        userNotes = notes
    )