package com.example.hastanghubaga.domain.model.settings

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

data class SupplementSettings(
    val supplementId: Long,
    val preferredServingSize: Double? = null,
    val preferredUnit: SupplementDoseUnit? = null,
    val preferredServingPerDay: Int? = null,
    val isEnabled: Boolean = true,
    val userNotes: String? = null
) {
    companion object {
        fun default(id: Long) = SupplementSettings(supplementId = id)
    }
}
