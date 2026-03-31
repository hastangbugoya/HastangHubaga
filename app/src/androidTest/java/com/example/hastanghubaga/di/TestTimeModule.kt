package com.example.hastanghubaga.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [TimeModule::class]
)
object TestTimeModule {

    @Provides
    @Singleton
    fun provideTestClock(): Clock =
        object : Clock {
            override fun now(): Instant =
                Instant.parse("2025-01-01T08:00:00Z")
        }
}