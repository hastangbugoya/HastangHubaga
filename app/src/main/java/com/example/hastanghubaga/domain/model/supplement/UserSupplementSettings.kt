package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

/**
 * Domain-level representation of user-specific supplement settings.
 *
 * This is mapped from SupplementUserSettingsEntity and is safe
 * to expose to UI and use cases.
 */
data class UserSupplementSettings(
    val preferredServingSize: Double?,
    val preferredUnit: SupplementDoseUnit?,
    val preferredServingsPerDay: Double?,
    val isEnabled: Boolean,
    val notes: String?
)
