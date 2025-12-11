package com.example.hastanghubaga.domain.repository.supplement
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.domain.model.settings.SupplementSettings
import kotlinx.coroutines.flow.*

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
                entity?.toDomain() ?: SupplementSettings.default(supplementId)
            }
            .distinctUntilChanged()
}
