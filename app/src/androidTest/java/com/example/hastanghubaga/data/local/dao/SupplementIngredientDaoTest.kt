package com.example.hastanghubaga.data.local.dao

import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementIngredientDao
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.*
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

@RunWith(AndroidJUnit4::class)
@SmallTest
class SupplementIngredientDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var supplementDao: SupplementEntityDao
    private lateinit var ingredientDao: IngredientEntityDao
    private lateinit var supplementIngredientDao: SupplementIngredientDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        supplementDao = db.supplementEntityDao()
        ingredientDao = db.ingredientEntityDao()
        supplementIngredientDao = db.supplementIngredientDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun makeSupplement(name: String) = SupplementEntity(
        name = name,
        brand = "BrandX",
        notes = null,
        recommendedServingSize = 2.0,
        recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
        servingsPerDay = 1.0,
        recommendedWithFood = null,
        recommendedLiquidInOz = null,
        recommendedTimeBetweenDailyDosesMinutes = null,
        avoidCaffeine = null,
        doseAnchorType = DoseAnchorType.MIDNIGHT,
        frequencyType = FrequencyType.DAILY,
        frequencyInterval = null,
        weeklyDays = null,
        offsetMinutes = null,
        customDose = null,
        customDoseUnit = null,
        startDate = null,
        lastTakenDate = null,
        isActive = true
    )

    private fun makeIngredient(name: String) = IngredientEntity(
        name = name,
        defaultUnit = IngredientUnit.MG,
        rdaValue = 100.0,
        rdaUnit = IngredientUnit.MG,
        upperLimitValue = 2000.0,
        upperLimitUnit = IngredientUnit.MG,
        category = "Vitamin"
    )

    // ----------------------------------------------------------
    // TESTS
    // ----------------------------------------------------------

    @Test
    fun insertLinks_savesDataAndRetrievable() = runBlocking {
        // Insert supplement
        val suppId = supplementDao.insertSupplement(makeSupplement("Multi"))

        // Insert ingredient
        val ingredientId = ingredientDao.insertIngredient(makeIngredient("Vitamin C"))

        // Insert link
        val link = SupplementIngredientEntity(
            supplementId = suppId,
            ingredientId = ingredientId,
            displayName = "VitC",
            amountPerServing = 500.0,
            unit = IngredientUnit.MG
        )

        supplementIngredientDao.insertLinks(listOf(link))

        val rows = supplementIngredientDao.getLinksForSupplement(suppId)
        Assert.assertEquals(1, rows.size)
        Assert.assertEquals("VitC", rows.first().displayName)
    }

    @Test
    fun getAllLinks_returnsAllInserted() = runBlocking {
        val supp1 = supplementDao.insertSupplement(makeSupplement("A"))
        val supp2 = supplementDao.insertSupplement(makeSupplement("B"))

        val ing1 = ingredientDao.insertIngredient(makeIngredient("C"))
        val ing2 = ingredientDao.insertIngredient(makeIngredient("D"))

        val list = listOf(
            SupplementIngredientEntity(
                supplementId = supp1,
                ingredientId = ing1,
                displayName = "C",
                amountPerServing = 100.0,
                unit = IngredientUnit.MG
            ),
            SupplementIngredientEntity(
                supplementId = supp2,
                ingredientId = ing2,
                displayName = "D",
                amountPerServing = 200.0,
                unit = IngredientUnit.MG
            )
        )

        supplementIngredientDao.insertLinks(list)

        val all = supplementIngredientDao.getAllLinks()
        Assert.assertEquals(2, all.size)
    }

    @Test
    fun foreignKeyConstraint_enforced() = runBlocking {
        val suppId = supplementDao.insertSupplement(makeSupplement("OnlySupplement"))

        // WARNING: ingredient not inserted — must cause FK fail
        val badLink = SupplementIngredientEntity(
            supplementId = suppId,
            ingredientId = 9999, // does NOT exist
            displayName = "Bad",
            amountPerServing = 123.0,
            unit = IngredientUnit.MG
        )

        try {
            supplementIngredientDao.insertLinks(listOf(badLink))
            Assert.fail("Expected FOREIGN KEY failure but did not occur")
        } catch (e: Exception) {
            Assert.assertTrue(e.message!!.contains("FOREIGN KEY"))
        }
    }
}
