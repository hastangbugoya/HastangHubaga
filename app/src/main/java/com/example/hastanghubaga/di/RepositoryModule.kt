package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSupplementRepository(
        supplementDao: SupplementEntityDao,
        ingredientEntityDao: IngredientEntityDao,
        supplementDailyLogDao: SupplementDailyLogDao,
        dailyStartTimeDao: DailyStartTimeDao
    ): SupplementRepository {
        return SupplementRepository(
            supplementDao,
            ingredientEntityDao,
            supplementDailyLogDao,
            dailyStartTimeDao
        )
    }
}
