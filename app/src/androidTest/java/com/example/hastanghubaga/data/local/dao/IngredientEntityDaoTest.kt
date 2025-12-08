package com.example.hastanghubaga.data.local.dao

import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject
import org.junit.Test

import com.google.common.truth.Truth.assertThat




@HiltAndroidTest
class IngredientEntityDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase
    private lateinit var dao: IngredientEntityDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = db.ingredientEntityDao()
    }

    private fun fakeIngredient(id: Long = 1) = IngredientEntity(
        id = id,
        name = "Creatine",
        defaultUnit = IngredientUnit.G,
        rdaValue = null,
        rdaUnit = null,
        upperLimitValue = null,
        upperLimitUnit = null,
        category = null
    )

    @Test
    fun insertIngredient_savesToDb() = runTest {
        val ing = fakeIngredient()
        dao.insertIngredient(ing)
        val loaded = dao.getIngredientById(1)

        assertThat(loaded).isEqualTo(ing)
    }

    @Test
    fun deleteIngredient_removesRow() = runTest {
        val ing = fakeIngredient()
        dao.insertIngredient(ing)
        dao.deleteIngredient(ing)

        val loaded = dao.getIngredientById(ing.id)
        assertThat(loaded).isNull()
    }
}
