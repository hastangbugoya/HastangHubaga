package com.example.hastanghubaga.domain.repository.supplement
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.mappers.toMealNutrition
import com.example.hastanghubaga.domain.model.settings.SupplementSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class LiveSupplementSettingsRepository(
    private val dao: SupplementUserSettingsDao
) {

    /**
     * Emits a NON-NULL SupplementSettings object.
     * - Emits immediate default
     * - Emits Room updates
     */
    fun observeSettings(supplementId: Long): Flow<SupplementSettings> =
        dao.observeSettings(supplementId)
            .map { entity ->
                entity?.toMealNutrition() ?: SupplementSettings.default(supplementId)
            }
            .distinctUntilChanged()
}
