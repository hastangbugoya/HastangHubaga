package com.example.hastanghubaga.di

import com.example.hastanghubaga.data.repository.NutritionAggregateRepositoryImpl
import com.example.hastanghubaga.data.repository.SupplementRepositoryImpl
import com.example.hastanghubaga.domain.repository.nutrition.NutritionAggregateRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSupplementRepository(
        impl: SupplementRepositoryImpl
    ): SupplementRepository

    @Binds
    abstract fun bindNutritionAggregateRepository(
        impl: NutritionAggregateRepositoryImpl
    ): NutritionAggregateRepository
}























//
//@Module
//@InstallIn(SingletonComponent::class)
//object RepositoryModule {
//    @Singleton
//    @Provides
//    fun provideSupplementRepository(
//        supplementDao: SupplementEntityDao,
//        ingredientEntityDao: IngredientEntityDao,
//        supplementDailyLogDao: SupplementDailyLogDao,
//        dailyStartTimeDao: DailyStartTimeDao,
//        eventTimeDao: EventTimeDao,
//        supplementUserSettingsDao: SupplementUserSettingsDao
//    ): SupplementRepository {
//        return SupplementRepositoryImpl(
//            supplementDao,
//            ingredientEntityDao,
//            supplementDailyLogDao,
//            dailyStartTimeDao,
//            eventTimeDao,
//            supplementUserSettingsDao
//        )
//    }
//
//    @Binds
//    abstract fun bindSupplementRepository(
//        impl: SupplementRepositoryImpl
//    ): SupplementRepository
//}
