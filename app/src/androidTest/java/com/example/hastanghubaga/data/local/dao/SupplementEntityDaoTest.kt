package com.example.hastanghubaga.data.local.dao

import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.*
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.time.DayOfWeek

@RunWith(AndroidJUnit4::class)
class SupplementEntityDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SupplementEntityDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.supplementEntityDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private fun makeSupplement(
        id: Long = 0,
        name: String = "Vitamin C",
        active: Boolean = true,
        offset: Int = 0
    ) = SupplementEntity(
        id = id,
        name = name,
        brand = "BrandX",
        notes = null,
        recommendedServingSize = 1.0,
        recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
        servingsPerDay = 1,
        recommendedWithFood = null,
        recommendedLiquidInOz = null,
        recommendedTimeBetweenDailyDosesMinutes = null,
        avoidCaffeine = null,
        doseAnchorType = DoseAnchorType.MIDNIGHT,
        frequencyType = FrequencyType.DAILY,
        frequencyInterval = null,
        weeklyDays = listOf(DayOfWeek.MONDAY),
        offsetMinutes = offset,
        customDose = null,
        customDoseUnit = null,
        startDate = null,
        lastTakenDate = null,
        isActive = active
    )

    // ---------------------------------------------------------
    // INSERT & UPDATE
    // ---------------------------------------------------------

    @Test
    fun insertSupplement_savesCorrectly() = runTest {
        val id = dao.insertSupplement(makeSupplement(name = "D3"))

        val loaded = dao.getSupplementById(id)
        Assert.assertNotNull(loaded)
        Assert.assertEquals("D3", loaded!!.name)
    }

    @Test
    fun insertSupplements_bulkInsertWorks() = runTest {
        val list = listOf(
            makeSupplement(name = "A"),
            makeSupplement(name = "B")
        )
        dao.insertSupplements(list)

        val all = dao.getAllSupplements()
        Assert.assertEquals(2, all.size)
    }

    @Test
    fun updateSupplement_changesValues() = runTest {
        val id = dao.insertSupplement(makeSupplement(name = "Before"))

        val updated = makeSupplement(id = id, name = "After")
        dao.updateSupplement(updated)

        val loaded = dao.getSupplementById(id)
        Assert.assertEquals("After", loaded!!.name)
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    fun deleteSupplement_removesRow() = runTest {
        val id = dao.insertSupplement(makeSupplement(name = "DeleteMe"))
        val entity = dao.getSupplementById(id)!!

        dao.deleteSupplement(entity)

        Assert.assertNull(dao.getSupplementById(id))
    }

    // ---------------------------------------------------------
    // FLOW READS
    // ---------------------------------------------------------

    @Test
    fun getAllSupplementsFlow_emitsValues() = runTest {
        dao.insertSupplement(makeSupplement(name = "FlowA"))
        dao.insertSupplement(makeSupplement(name = "FlowB"))

        val list = dao.getAllSupplementsFlow().first()
        Assert.assertEquals(2, list.size)
    }

    @Test
    fun getActiveSupplementsFlow_filtersCorrectly() = runTest {
        dao.insertSupplement(makeSupplement(name = "Active1", active = true))
        dao.insertSupplement(makeSupplement(name = "InactiveX", active = false))

        val active = dao.getActiveSupplementsFlow().first()
        Assert.assertEquals(1, active.size)
        Assert.assertEquals("Active1", active[0].name)
    }

    // ---------------------------------------------------------
    // ORDERED QUERY
    // ---------------------------------------------------------

    @Test
    fun getActiveSupplementsOrderedByOffset_ordersCorrectly() = runTest {
        dao.insertSupplements(
            listOf(
                makeSupplement(name = "C", offset = 30),
                makeSupplement(name = "B", offset = 20),
                makeSupplement(name = "A", offset = 10)
            )
        )

        val list = dao.getActiveSupplementsOrderedByOffset()
        Assert.assertEquals(listOf("A", "B", "C"), list.map { it.name })
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    @Test
    fun searchSupplements_returnsMatches() = runTest {
        dao.insertSupplement(makeSupplement(name = "Fish Oil"))
        dao.insertSupplement(makeSupplement(name = "Vitamin D"))
        dao.insertSupplement(makeSupplement(name = "Probiotic"))

        val results = dao.searchSupplements("Vit").first()
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Vitamin D", results[0].name)
    }

    // ---------------------------------------------------------
    // RELATIONS — SupplementWithIngredients
    // ---------------------------------------------------------

    @Test
    fun getSupplementWithIngredients_returnsData() = runTest {
        val supplementId = dao.insertSupplement(makeSupplement(name = "Multi"))

        // Insert a real ingredient first
        val ingredientDao = db.ingredientEntityDao()
        val ingredientId = ingredientDao.insertIngredient(
            IngredientEntity(
                id = 0,
                name = "Vitamin C",
                defaultUnit = IngredientUnit.MG,
                rdaValue = 90.0,
                rdaUnit = IngredientUnit.MG,
                upperLimitValue = 2000.0,
                upperLimitUnit = IngredientUnit.MG,
                category = "Vitamin"
            )
        )

        // Now insert supplement–ingredient link
        val ingDao = db.supplementIngredientDao()
        ingDao.insertLinks(
            listOf(
                SupplementIngredientEntity(
                    supplementId = supplementId,
                    ingredientId = ingredientId, // must match FK
                    displayName = "VitC",
                    amountPerServing = 500.0,
                    unit = IngredientUnit.MG
                )
            )
        )

        val withIng = dao.getSupplementWithIngredients(supplementId)
        Assert.assertNotNull(withIng)
        Assert.assertEquals(1, withIng!!.ingredients.size)
    }


    // ---------------------------------------------------------
    // RELATIONS — SupplementWithSettings
    // ---------------------------------------------------------

    @Test
    fun observeSupplementWithSettings_emitsJoinedData() = runTest {
        val id = dao.insertSupplement(makeSupplement(name = "Omega3"))

        db.supplementUserSettingsDao().upsert(
            SupplementUserSettingsEntity(
                supplementId = id,
                preferredServingSize = 2.0,
                preferredUnit = SupplementDoseUnit.CAPSULE,
                preferredServingPerDay = 1,
                isEnabled = true,
                userNotes = "Take with food"
            )
        )

        val joined = dao.observeSupplementWithSettings(id).first()
        Assert.assertNotNull(joined)
        Assert.assertEquals(id, joined!!.supplement.id)
        Assert.assertEquals(2.0, joined.settings?.preferredServingSize)
    }

    @Test
    fun getSupplementWithSettings_returnsCorrectJoin() = runTest {
        val id = dao.insertSupplement(makeSupplement(name = "Zinc"))

        db.supplementUserSettingsDao().upsert(
            SupplementUserSettingsEntity(
                supplementId = id,
                preferredServingSize = 50.0,
                preferredUnit = SupplementDoseUnit.MG
            )
        )

        val joined = dao.getSupplementWithSettings(id)
        Assert.assertNotNull(joined)
        Assert.assertEquals("Zinc", joined!!.supplement.name)
        Assert.assertEquals(50.0, joined.settings?.preferredServingSize)
    }

    // ---------------------------------------------------------
    // BACKUP SUPPORT
    // ---------------------------------------------------------

    @Test
    fun insertAll_insertsAllRows() = runTest {
        val list = listOf(
            makeSupplement(name = "X1"),
            makeSupplement(name = "X2"),
            makeSupplement(name = "X3")
        )

        dao.insertAll(list)

        val all = dao.getAllOnce()
        Assert.assertEquals(3, all.size)
    }
}
