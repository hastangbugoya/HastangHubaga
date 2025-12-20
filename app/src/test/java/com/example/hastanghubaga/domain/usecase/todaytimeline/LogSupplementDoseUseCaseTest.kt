package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.factory.FakeClock
import com.example.hastanghubaga.factory.FakeSupplementLogRepository
import kotlinx.datetime.Instant
import org.junit.Before
import java.time.LocalDateTime

class LogSupplementDoseUseCaseTest {

    private lateinit var repository: FakeSupplementLogRepository
    private lateinit var clock: FakeClock
    private lateinit var useCase: LogSupplementDoseUseCase

    @Before
    fun setUp() {
        repository = FakeSupplementLogRepository()
        clock = FakeClock(
            Instant.parse("2025-12-20T10:30:00Z")
        )
        useCase = LogSupplementDoseUseCase(
            repository = repository,
            clock = clock
        )
    }
}
