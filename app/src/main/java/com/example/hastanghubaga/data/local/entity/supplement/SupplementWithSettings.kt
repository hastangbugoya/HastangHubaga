package com.example.hastanghubaga.data.local.entity.supplement

import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity

data class SupplementWithSettings(
    val supplement: SupplementEntity,
    val settings: SupplementUserSettingsEntity? // may be null if user hasn't customized it
) {

    /** Returns the user’s chosen dose amount if available, otherwise the default supplement dose */
    val effectiveDosePerServing: Double
        get() = settings?.preferredServingSize
                ?:  supplement.recommendedServingSize

    /** If the user reduces servings per day, reflect it. Otherwise fallback. */
    val effectiveServingsPerDay: Int
        get() = settings?.preferredServingPerDay ?: supplement.servingsPerDay

    /** Whether the user disabled this supplement entirely */
    val isActive: Boolean
        get() = settings?.isEnabled ?: supplement.isActive

    /** Optional user note for this supplement */
    val userNote: String?
        get() = settings?.userNotes
}
