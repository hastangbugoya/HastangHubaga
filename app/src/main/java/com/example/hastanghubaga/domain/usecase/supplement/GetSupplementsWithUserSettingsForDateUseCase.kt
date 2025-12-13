package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetSupplementsWithUserSettingsForDateUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository
) {

    /**
     * Executes the use case.
     *
     * @param date The date for which supplements should be retrieved.
     * @return A list of `SupplementWithUserSettings` items active on that date.
     */
    suspend operator fun invoke(date: LocalDate): Flow<List<SupplementWithUserSettings>> {
        // TODO: Apply full scheduling logic here
        return supplementRepository.getSupplementsForDate(date.toString())
    }
}