package com.example.hastanghubaga.factory

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FakeClock(
    private val instant: Instant
) : Clock {
    override fun now(): Instant = instant
}