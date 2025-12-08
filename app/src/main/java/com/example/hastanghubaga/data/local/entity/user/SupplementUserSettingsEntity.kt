package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

@Entity(tableName = "supplement_user_settings")
data class SupplementUserSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val supplementId: Long,

    /** User overrides recommended dosage */
    val preferredServingSize: Double? = null,
    val preferredUnit: SupplementDoseUnit? = null,

    val preferredServingPerDay: Int? = null,


    /** Whether the user wants this supplement active */
    val isEnabled: Boolean = true,

    /** Optional note the user adds */
    val userNotes: String? = null
)
