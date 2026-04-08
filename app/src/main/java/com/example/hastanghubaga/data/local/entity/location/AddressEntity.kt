package com.example.hastanghubaga.data.local.entity.location

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Reusable saved address/location record for future cross-feature location support.
 *
 * This table is intentionally lightweight for the initial DB foundation bump-up.
 * It gives the app a stable saved-location identifier that can later be referenced
 * by activities, occurrences, logs, and other features without forcing the app to
 * rely only on raw free-text addresses.
 *
 * Current purpose:
 * - support favorite/saved locations
 * - support future map-intent launching
 * - allow entities to reference a reusable location by id
 *
 * Important:
 * - This does not replace raw string fallback storage
 * - Raw fallback strings should still be stored on feature rows where needed
 * - Editing logic should later enforce one active source at a time:
 *   savedAddressId OR addressAsRawString
 *
 * Future AI/dev reminder:
 * This entity is deliberately conservative. Do not over-expand it unless a real
 * use case requires it. Richer metadata like lat/lng, placeId, labels, notes,
 * structured address parts, and map-provider fields can be added later once the
 * app's resolution and editing flows are fully established.
 */
@Entity(
    tableName = "addresses",
    indices = [
        Index(value = ["label"]),
        Index(value = ["fullAddress"])
    ]
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Optional user-facing nickname such as "Home", "Gym", or "Mom's House".
     */
    val label: String? = null,

    /**
     * Human-readable address or location text to display and potentially pass to
     * future map intents. This may be a formal street address or any user-entered
     * place description the user wants to save.
     */
    val fullAddress: String,

    /**
     * Whether this saved address should be treated as a user favorite.
     */
    val isFavorite: Boolean = false,

    /**
     * Creation timestamp for stable ordering and future auditing/debugging.
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Update timestamp for future edit tracking.
     */
    val updatedAt: Long = System.currentTimeMillis()
)