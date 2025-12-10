package com.example.hastanghubaga.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import com.example.hastanghubaga.data.local.dao.supplement.DailyStartTimeDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyStartTimeDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DailyStartTimeDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.dailyStartTimeDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ------------------------------------------------------------
    // TEST: upsert() inserts a new row
    // ------------------------------------------------------------
    @Test
    fun upsert_insertsNewRow() = runTest {
        val entry = DailyStartTimeEntity(
            date = "2025-01-01",
            hourZero = 3600
        )

        dao.upsert(entry)

        val loaded = dao.getStartTime("2025-01-01")
        assertNotNull(loaded)
        assertEquals(3600, loaded!!.hourZero)
    }

    // ------------------------------------------------------------
    // TEST: upsert() updates existing row
    // ------------------------------------------------------------
    @Test
    fun upsert_updatesExistingRow() = runTest {
        val original = DailyStartTimeEntity("2025-01-01", 3600)
        val updated = DailyStartTimeEntity("2025-01-01", 7200)

        dao.upsert(original)
        dao.upsert(updated)

        val loaded = dao.getStartTime("2025-01-01")
        assertEquals(7200, loaded!!.hourZero)
    }

    // ------------------------------------------------------------
    // TEST: setStartTime() replaces row
    // ------------------------------------------------------------
    @Test
    fun setStartTime_replacesExistingRow() = runTest {
        dao.setStartTime(DailyStartTimeEntity("2025-01-01", 1000))
        dao.setStartTime(DailyStartTimeEntity("2025-01-01", 2000))

        val loaded = dao.getStartTime("2025-01-01")
        assertEquals(2000, loaded!!.hourZero)
    }

    // ------------------------------------------------------------
    // TEST: cleanup() removes older rows
    // ------------------------------------------------------------
    @Test
    fun cleanup_removesOlderDates() = runTest {
        // Insert 3 dates
        dao.upsert(DailyStartTimeEntity("2025-01-01", 1000))
        dao.upsert(DailyStartTimeEntity("2025-01-02", 2000))
        dao.upsert(DailyStartTimeEntity("2025-01-03", 3000))

        dao.cleanup("2025-01-02") // delete rows < this

        val all = dao.getAll()
        assertEquals(2, all.size)
        assertTrue(all.any { it.date == "2025-01-02" })
        assertTrue(all.any { it.date == "2025-01-03" })
    }

    // ------------------------------------------------------------
    // TEST: insertAll() inserts multiple rows
    // ------------------------------------------------------------
    @Test
    fun insertAll_insertsMultiple() = runTest {
        val entries = listOf(
            DailyStartTimeEntity("2025-02-01", 1000),
            DailyStartTimeEntity("2025-02-02", 2000),
            DailyStartTimeEntity("2025-02-03", 3000)
        )

        dao.insertAll(entries)

        val loaded = dao.getAll()
        assertEquals(3, loaded.size)
    }

    // ------------------------------------------------------------
    // TEST: getAll() returns all rows
    // ------------------------------------------------------------
    @Test
    fun getAll_returnsEverything() = runTest {
        dao.upsert(DailyStartTimeEntity("2025-03-01", 1111))
        dao.upsert(DailyStartTimeEntity("2025-03-02", 2222))

        val loaded = dao.getAll()

        assertEquals(2, loaded.size)
        assertTrue(loaded.any { it.hourZero == 1111 })
        assertTrue(loaded.any { it.hourZero == 2222 })
    }
}
