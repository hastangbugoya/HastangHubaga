package com.example.hastanghubaga.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [TimeModule::class]
)
object TestTimeModule {

    @Provides
    @Singleton
    fun provideTestClock(): Clock =
        Clock.fixed(
            Instant.parse("2025-01-01T08:00:00Z"),
            ZoneOffset.UTC
        )
}