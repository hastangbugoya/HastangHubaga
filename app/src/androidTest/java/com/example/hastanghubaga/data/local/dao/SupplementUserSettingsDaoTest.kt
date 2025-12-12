package com.example.hastanghubaga.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hastanghubaga.data.local.dao.user.SupplementUserSettingsDao
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SupplementUserSettingsDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dao: SupplementUserSettingsDao

    @Before
    fun setup() = hiltRule.inject()

    private fun makeSettings(id: Long, dose: Double) =
        SupplementUserSettingsEntity(
            supplementId = id,
            preferredServingSize = dose,
            preferredUnit = SupplementDoseUnit.CAPSULE,
            preferredServingPerDay = 1,
            isEnabled = true,
            userNotes = null
        )

    @Test
    fun upsert_inserts_and_updates() = runTest {
        dao.upsert(makeSettings(1, 2.0))
        dao.upsert(makeSettings(1, 10.0))

        val stored = dao.getSettings(1)
        Assert.assertEquals(10.0, stored?.preferredServingSize)
    }

    @Test
    fun observeSettings_emits_values() = runTest {
        val emissionsDeferred = async {
            dao.observeSettings(1).take(2).toList()
        }

        dao.upsert(makeSettings(1, 2.0))
        dao.upsert(makeSettings(1, 10.0))

        val emissions = emissionsDeferred.await()

        Assert.assertEquals(2.0, emissions[0]?.preferredServingSize)
        Assert.assertEquals(10.0, emissions[1]?.preferredServingSize)
    }
}
