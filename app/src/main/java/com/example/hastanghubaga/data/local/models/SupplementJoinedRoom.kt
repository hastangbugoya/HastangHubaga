package com.example.hastanghubaga.data.local.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementWithSettings
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity

data class SupplementJoinedRoom(
    @Embedded val supplement: SupplementEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "supplementId"
    )
    val settings: SupplementUserSettingsEntity?
)

fun SupplementJoinedRoom.toDomain(): SupplementWithSettings =
    SupplementWithSettings(
        supplement = supplement,
        settings = settings
    )

fun SupplementJoinedRoom?.toDomainSafe(): SupplementWithSettings? {
    return this?.toDomain()
}