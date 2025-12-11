package com.example.hastanghubaga.data.local.dao

import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.hastanghubaga.data.local.db.TestAppDatabase
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.repository.supplement.LiveSupplementSettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.*
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@SmallTest
class SupplementUserSettingsDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject lateinit var db: TestAppDatabase
    @Inject lateinit var dao: SupplementUserSettingsDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun makeSettings(
        supplementId: Long,
        dose: Double? = null,
        enabled: Boolean = true
    ) = SupplementUserSettingsEntity(
        supplementId = supplementId,
        preferredServingSize = dose,
        preferredUnit = SupplementDoseUnit.CAPSULE,
        preferredServingPerDay = 1,
        isEnabled = enabled,
        userNotes = null
    )

    // ---------------------------------------------------------
    // TESTS
    // ---------------------------------------------------------

    @Test
    fun upsert_insertsRow() = runBlocking {
        val settings = makeSettings(supplementId = 1L)

        dao.upsert(settings)

        val stored = dao.getSettings(1L)
        Assert.assertNotNull(stored)
        Assert.assertEquals(1L, stored!!.supplementId)
    }

    @Test
    fun upsert_replacesExisting() = runBlocking {
        dao.upsert(makeSettings(1L, dose = 2.0))
        dao.upsert(makeSettings(1L, dose = 5.0)) // replace

        val stored = dao.getSettings(1L)
        Assert.assertEquals(5.0, stored?.preferredServingSize ?: -1.0, 0.01)
    }

    @Test
    fun getSettings_returnsNullWhenMissing() = runBlocking {
        val result = dao.getSettings(99L)
        Assert.assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun observeSettings_emitsUpdates() = runTest {

        val emissionsDeferred = async {
            dao.observeSettings(1L)
                .take(2)
                .toList()
        }

        dao.upsert(makeSettings(1L, dose = 2.0))
        dao.upsert(makeSettings(1L, dose = 10.0))

        val emissions = emissionsDeferred.await()

        println("MEOW emissions = $emissions")

        Assert.assertEquals(2, emissions.size)
        Assert.assertEquals(2.0, emissions[0]!!.preferredServingSize)
        Assert.assertEquals(10.0, emissions[1]!!.preferredServingSize)
    }

    @Test
    fun deleteSettings_removesRow() = runBlocking {
        dao.upsert(makeSettings(1L))
        dao.deleteSettings(1L)

        val stored = dao.getSettings(1L)
        Assert.assertNull(stored)
    }

    @Test
    fun getAll_returnsAllRows() = runBlocking {
        dao.upsert(makeSettings(1L))
        dao.upsert(makeSettings(2L))
        dao.upsert(makeSettings(3L))

        val list = dao.getAll()
        Assert.assertEquals(3, list.size)
    }

    @Test
    fun insertAll_insertsMultipleRows() = runBlocking {
        val list = listOf(
            makeSettings(1L, enabled = true),
            makeSettings(2L, enabled = false),
            makeSettings(3L, enabled = true)
        )

        dao.insertAll(list)

        val stored = dao.getAll()
        Assert.assertEquals(3, stored.size)
    }
}
