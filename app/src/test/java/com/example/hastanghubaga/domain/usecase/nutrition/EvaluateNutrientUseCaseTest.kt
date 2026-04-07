package com.example.hastanghubaga.domain.usecase.nutrition

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateNutrientUseCaseTest {

    private val useCase = EvaluateNutrientUseCase()

    @Test
    fun `intake within min and max passes`() = runTest {
        val result = useCase(
            nutrientKey = "PROTEIN_G",
            intake = 120.0,
            min = 100.0,
            target = 130.0,
            max = 150.0
        )

        assertTrue(result.isWithinRange)
        assertFalse(result.isBelowMin)
        assertFalse(result.isAboveMax)
        assertFalse(result.isMissing)
    }

    @Test
    fun `intake below min fails`() = runTest {
        val result = useCase(
            nutrientKey = "PROTEIN_G",
            intake = 80.0,
            min = 100.0,
            target = 130.0,
            max = 150.0
        )

        assertFalse(result.isWithinRange)
        assertTrue(result.isBelowMin)
        assertFalse(result.isAboveMax)
        assertFalse(result.isMissing)
    }

    @Test
    fun `intake above max fails`() = runTest {
        val result = useCase(
            nutrientKey = "SODIUM_MG",
            intake = 2600.0,
            min = null,
            target = 2000.0,
            max = 2300.0
        )

        assertFalse(result.isWithinRange)
        assertFalse(result.isBelowMin)
        assertTrue(result.isAboveMax)
        assertFalse(result.isMissing)
    }

    @Test
    fun `missing intake with min fails`() = runTest {
        val result = useCase(
            nutrientKey = "FIBER_G",
            intake = null,
            min = 25.0,
            target = 30.0,
            max = null
        )

        assertFalse(result.isWithinRange)
        assertFalse(result.isBelowMin)
        assertFalse(result.isAboveMax)
        assertTrue(result.isMissing)
    }

    @Test
    fun `missing intake with max only passes`() = runTest {
        val result = useCase(
            nutrientKey = "SODIUM_MG",
            intake = null,
            min = null,
            target = null,
            max = 2300.0
        )

        assertTrue(result.isWithinRange)
        assertFalse(result.isBelowMin)
        assertFalse(result.isAboveMax)
        assertTrue(result.isMissing)
    }

    @Test
    fun `missing intake with no constraints passes`() = runTest {
        val result = useCase(
            nutrientKey = "VITAMIN_C_MG",
            intake = null,
            min = null,
            target = null,
            max = null
        )

        assertTrue(result.isWithinRange)
        assertFalse(result.isBelowMin)
        assertFalse(result.isAboveMax)
        assertTrue(result.isMissing)
    }

    @Test
    fun `intake equal to min passes`() = runTest {
        val result = useCase(
            nutrientKey = "PROTEIN_G",
            intake = 100.0,
            min = 100.0,
            target = 130.0,
            max = 150.0
        )

        assertTrue(result.isWithinRange)
        assertFalse(result.isBelowMin)
        assertFalse(result.isAboveMax)
        assertFalse(result.isMissing)
    }

    @Test
    fun `intake equal to max passes`() = runTest {
        val result = useCase(
            nutrientKey = "SODIUM_MG",
            intake = 2300.0,
            min = null,
            target = 2000.0,
            max = 2300.0
        )

        assertTrue(result.isWithinRange)
        assertFalse(result.isBelowMin)
        assertFalse(result.isAboveMax)
        assertFalse(result.isMissing)
    }
}