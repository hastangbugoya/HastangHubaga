package com.example.hastanghubaga.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTimeDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: EventTimeDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.eventTimeDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ---------------------------------------------------------
    // DEFAULT TIME TESTS
    // ---------------------------------------------------------

    @Test
    fun upsertDefault_insertsAndUpdates() = runTest {
        val anchor = DoseAnchorType.BREAKFAST

        dao.upsertDefault(EventDefaultTimeEntity(anchor, 1000))
        dao.upsertDefault(EventDefaultTimeEntity(anchor, 2000))

        val loaded = dao.getDefault(anchor)
        assertNotNull(loaded)
        assertEquals(2000, loaded!!.timeSeconds)
    }

    @Test
    fun getAllDefaults_returnsAllEntries() = runTest {
        dao.upsertDefault(EventDefaultTimeEntity(DoseAnchorType.BREAKFAST, 1000))
        dao.upsertDefault(EventDefaultTimeEntity(DoseAnchorType.LUNCH, 2000))
        dao.upsertDefault(EventDefaultTimeEntity(DoseAnchorType.DINNER, 3000))

        val list = dao.getAllDefaults()
        assertEquals(3, list.size)
    }

    // ---------------------------------------------------------
    // DAILY OVERRIDE TESTS
    // ---------------------------------------------------------

    @Test
    fun upsertOverride_insertsAndUpdates() = runTest {
        val date = "2025-05-01"
        val anchor = DoseAnchorType.LUNCH

        dao.upsertOverride(EventDailyOverrideEntity(date, anchor, 500))
        dao.upsertOverride(EventDailyOverrideEntity(date, anchor, 1500))

        val loaded = dao.getOverride(date, anchor)
        assertNotNull(loaded)
        assertEquals(1500, loaded!!.timeSeconds)
    }

    @Test
    fun removeOverride_deletesCorrectRow() = runTest {
        val date = "2025-05-01"
        val anchor = DoseAnchorType.DINNER

        dao.upsertOverride(EventDailyOverrideEntity(date, anchor, 900))
        dao.removeOverride(date, anchor)

        val loaded = dao.getOverride(date, anchor)
        assertNull(loaded)
    }

    @Test
    fun getAllOverrides_returnsAll() = runTest {
        dao.upsertOverride(EventDailyOverrideEntity("2025-01-01", DoseAnchorType.BREAKFAST, 100))
        dao.upsertOverride(EventDailyOverrideEntity("2025-01-01", DoseAnchorType.LUNCH, 200))
        dao.upsertOverride(EventDailyOverrideEntity("2025-01-02", DoseAnchorType.DINNER, 300))

        val list = dao.getAllOverrides()
        assertEquals(3, list.size)
    }

    // ---------------------------------------------------------
    // BACKUP TESTS
    // ---------------------------------------------------------

    @Test
    fun insertAllDefault_insertsMultiple() = runTest {
        val list = listOf(
            EventDefaultTimeEntity(DoseAnchorType.BREAKFAST, 1000),
            EventDefaultTimeEntity(DoseAnchorType.LUNCH, 2000),
            EventDefaultTimeEntity(DoseAnchorType.DINNER, 3000)
        )

        dao.insertAllDefault(list)

        val all = dao.getAllDefaults()
        assertEquals(3, all.size)
    }

    @Test
    fun insertAllOverrides_insertsMultiple() = runTest {
        val list = listOf(
            EventDailyOverrideEntity("2025-01-01", DoseAnchorType.BREAKFAST, 500),
            EventDailyOverrideEntity("2025-01-01", DoseAnchorType.LUNCH, 600)
        )

        dao.insertAllOverrides(list)

        val all = dao.getAllOverrides()
        assertEquals(2, all.size)
    }

    @Test
    fun insertAll_aliasAlsoWorks() = runTest {
        val list = listOf(
            EventDailyOverrideEntity("2025-03-01", DoseAnchorType.DINNER, 700),
            EventDailyOverrideEntity("2025-03-01", DoseAnchorType.CAFFEINE, 800)
        )

        dao.insertAll(list)

        val all = dao.getAllOverrides()
        assertEquals(2, all.size)
    }
}
