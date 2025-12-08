package com.example.hastanghubaga.data.local.entity.supplement

import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity

/**
 * Combines a [SupplementEntity] with optional user-defined override settings.
 *
 * This model represents the *effective* configuration the app should follow
 * when scheduling, displaying, or calculating doses for a supplement.
 *
 * ## Purpose
 * A supplement may have recommended defaults (e.g., 2 capsules, twice daily).
 * The user may override any of the following via [SupplementUserSettingsEntity]:
 * - Preferred serving size
 * - Preferred number of servings per day
 * - Whether the supplement is enabled/disabled
 * - Optional personal notes
 *
 * This wrapper merges both layers and exposes computed, ready-to-use values
 * for UI and domain logic.
 *
 * ## Behavior Summary
 * - If the user has not customized a supplement, all values fall back to the
 *   defaults defined in [SupplementEntity].
 * - If user settings exist, they override only the fields the user changed.
 *
 * ## Typical Use Cases
 * - Displaying supplement details on the Today screen
 * - Calculating daily ingredient totals based on user-modified dosages
 * - Determining whether a supplement should appear in schedules or reminders
 *
 * ## Notes
 * This is a **domain model**, not a Room relation.
 * The data layer should expose [SupplementJoinedRoom] (Room relation) and map
 * it to this domain class using `.toDomain()`.
 */
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
