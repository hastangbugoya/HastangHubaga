package com.example.hastanghubaga.data.local.db.callback

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.example.hastanghubaga.data.local.dao.supplement.IngredientEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementIngredientDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseCallbackTest {

    private lateinit var db: AppDatabase

    private lateinit var supplementDao: SupplementEntityDao
    private lateinit var ingredientDao: IngredientEntityDao
    private lateinit var linkDao: SupplementIngredientDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .addCallback(DatabaseCallback())   // YOUR callback
            .allowMainThreadQueries()
            .build()

        supplementDao = db.supplementEntityDao()
        ingredientDao = db.ingredientEntityDao()
        linkDao = db.supplementIngredientDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // -----------------------------------------------------------
    // TEST 1 — verify supplements inserted
    // -----------------------------------------------------------
    @Test
    fun callback_insertsSupplements() = runBlocking {
        val activeSupps = supplementDao.getActiveSupplements().first()
        Assert.assertTrue("Expected prepopulated active supplements", activeSupps.isNotEmpty())
    }

    @Test
    fun debugPrintAllSupplements() = runBlocking {
        val all = supplementDao.getActiveSupplements().first()
        println("DEBUG: supplements in DB -> $all")
    }

    // -----------------------------------------------------------
    // TEST 2 — verify ingredients inserted
    // -----------------------------------------------------------
    @Test
    fun callback_insertsIngredients() = runBlocking {
        val ingredients = ingredientDao.getAllIngredients()
        Assert.assertTrue("Expected prepopulated ingredients", ingredients.isNotEmpty())
    }

    // -----------------------------------------------------------
    // TEST 3 — verify linking table inserted
    // -----------------------------------------------------------
    @Test
    fun callback_insertsSupplementIngredientLinks() = runBlocking {
        val links = linkDao.getAllLinks()
        Assert.assertTrue("Expected linking table rows from callback", links.isNotEmpty())
    }
}