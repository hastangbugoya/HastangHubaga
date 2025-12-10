package com.example.hastanghubaga.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IngredientEntityDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: IngredientEntityDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.ingredientEntityDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // -------------------------------------------------------------
    // INSERT TESTS
    // -------------------------------------------------------------

    @Test
    fun insertIngredient_insertsSuccessfully() = runTest {
        val id = dao.insertIngredient(
            IngredientEntity(name = "Vitamin C", defaultUnit = IngredientUnit.MG)
        )

        val loaded = dao.getIngredientById(id)
        Assert.assertNotNull(loaded)
        Assert.assertEquals("Vitamin C", loaded!!.name)
    }

    @Test
    fun insertIngredients_bulkInsertWorks() = runTest {
        val list = listOf(
            IngredientEntity(name = "Magnesium", defaultUnit = IngredientUnit.MG),
            IngredientEntity(name = "Zinc", defaultUnit = IngredientUnit.MG)
        )

        dao.insertIngredients(list)

        val all = dao.getAllIngredients()
        Assert.assertEquals(2, all.size)
    }

    @Test
    fun insertIngredientsReturningIds_returnsCorrectIds() = runTest {
        val items = listOf(
            IngredientEntity(name = "Iron", defaultUnit = IngredientUnit.MG),
            IngredientEntity(name = "Selenium", defaultUnit = IngredientUnit.MCG)
        )

        val ids = dao.insertIngredientsReturningIds(items)

        Assert.assertEquals(2, ids.size)

        val loaded = dao.getAllOnce()
        Assert.assertEquals(2, loaded.size)
    }

    @Test
    fun insertAll_nonSuspendAlsoWorks() = runTest {
        val items = listOf(
            IngredientEntity(name = "Copper", defaultUnit = IngredientUnit.MG),
            IngredientEntity(name = "Chromium", defaultUnit = IngredientUnit.MCG)
        )

        dao.insertAll(items)

        Assert.assertEquals(2, dao.getAllOnce().size)
    }

    // -------------------------------------------------------------
    // GET TESTS
    // -------------------------------------------------------------

    @Test
    fun getIngredientByName_findsCorrectItem() = runTest {
        dao.insertIngredient(IngredientEntity(name = "Vitamin D", defaultUnit = IngredientUnit.IU))

        val loaded = dao.getIngredientByName("Vitamin D")
        Assert.assertNotNull(loaded)
        Assert.assertEquals(IngredientUnit.IU, loaded!!.defaultUnit)
    }

    @Test
    fun getAllIngredientsFlow_emitsValues() = runTest {
        dao.insertIngredient(IngredientEntity(name = "A", defaultUnit = IngredientUnit.MG))
        dao.insertIngredient(IngredientEntity(name = "B", defaultUnit = IngredientUnit.MG))

        val items = dao.getAllIngredientsFlow().first()
        Assert.assertEquals(2, items.size)
    }

    @Test
    fun searchIngredients_findsMatching() = runTest {
        dao.insertIngredient(IngredientEntity(name = "Calcium Citrate", defaultUnit = IngredientUnit.MG))
        dao.insertIngredient(IngredientEntity(name = "Calcium Carbonate", defaultUnit = IngredientUnit.MG))
        dao.insertIngredient(IngredientEntity(name = "Boron", defaultUnit = IngredientUnit.MG))

        val results = dao.searchIngredients("Calcium").first()

        Assert.assertEquals(2, results.size)
        Assert.assertTrue(results.all { it.name.contains("Calcium") })
    }

    // -------------------------------------------------------------
    // UPDATE TESTS
    // -------------------------------------------------------------

    @Test
    fun updateIngredient_updatesFullRow() = runTest {
        val id = dao.insertIngredient(IngredientEntity(name = "Omega 3", defaultUnit = IngredientUnit.G))

        val updated = IngredientEntity(
            id = id,
            name = "Omega-3 Fish Oil",
            defaultUnit = IngredientUnit.G
        )

        dao.updateIngredient(updated)

        val loaded = dao.getIngredientById(id)
        Assert.assertEquals("Omega-3 Fish Oil", loaded!!.name)
    }

    @Test
    fun updateRda_updatesOnlyRdaFields() = runTest {
        val id = dao.insertIngredient(IngredientEntity(name = "Biotin", defaultUnit = IngredientUnit.MCG))

        dao.updateRda(id, 300.0, IngredientUnit.MCG.name)

        val loaded = dao.getIngredientById(id)
        Assert.assertEquals(300.0, loaded?.rdaValue ?: 0.0, 0.0)
        Assert.assertEquals(IngredientUnit.MCG, loaded?.rdaUnit)
    }

    @Test
    fun updateUpperLimit_updatesULOnly() = runTest {
        val id = dao.insertIngredient(IngredientEntity(name = "Vitamin A", defaultUnit = IngredientUnit.IU))

        dao.updateUpperLimit(id, 5000.0, IngredientUnit.IU.name)

        val loaded = dao.getIngredientById(id)
        Assert.assertEquals(5000.0, loaded?.upperLimitValue ?: 0.0, 0.0)
        Assert.assertEquals(IngredientUnit.IU, loaded?.upperLimitUnit)
    }

    @Test
    fun updateCategory_updatesOnlyCategory() = runTest {
        val id = dao.insertIngredient(IngredientEntity(name = "Ashwagandha", defaultUnit = IngredientUnit.MG))

        dao.updateCategory(id, "Herb")

        val loaded = dao.getIngredientById(id)
        Assert.assertEquals("Herb", loaded!!.category)
    }

    // -------------------------------------------------------------
    // DELETE TESTS
    // -------------------------------------------------------------

    @Test
    fun deleteIngredient_removesRow() = runTest {
        val id = dao.insertIngredient(IngredientEntity(name = "K2", defaultUnit = IngredientUnit.MCG))

        val entity = dao.getIngredientById(id)!!
        dao.deleteIngredient(entity)

        Assert.assertNull(dao.getIngredientById(id))
    }

    @Test
    fun clearAllIngredients_removesAll() = runTest {
        dao.insertIngredient(IngredientEntity(name = "X", defaultUnit = IngredientUnit.MG))
        dao.insertIngredient(IngredientEntity(name = "Y", defaultUnit = IngredientUnit.MG))

        dao.clearAllIngredients()

        Assert.assertEquals(0, dao.getAllIngredients().size)
    }
}
