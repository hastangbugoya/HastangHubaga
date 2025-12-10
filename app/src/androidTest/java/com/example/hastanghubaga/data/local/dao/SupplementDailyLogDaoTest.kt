package com.example.hastanghubaga.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupplementDailyLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SupplementDailyLogDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.supplementDailyLogDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // -------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------

    private fun makeLog(
        id: Long = 0L,
        supplementId: Long,
        date: String,
        fraction: Double,
        timestamp: Long
    ) = SupplementDailyLogEntity(
        id = id,
        supplementId = supplementId,
        date = date,
        actualServingTaken = fraction,
        doseUnit = SupplementDoseUnit.CAPSULE,
        timestamp = timestamp
    )

    // -------------------------------------------------------------
    // INSERT TESTS
    // -------------------------------------------------------------

    @Test
    fun insertDoseLog_insertsSuccessfully() = runTest {
        val id = dao.insertDoseLog(makeLog(supplementId = 1, date = "2025-01-01", fraction = 1.0, timestamp = 100))

        val loaded = dao.getDoseLogById(id)
        Assert.assertNotNull(loaded)
        Assert.assertEquals(1L, loaded!!.supplementId)
        Assert.assertEquals(1.0, loaded.actualServingTaken, 0.0)
    }

    @Test
    fun insertDoseLogs_bulkInsertWorks() = runTest {
        val logs = listOf(
            makeLog(supplementId = 1, date = "2025-01-01", fraction = 1.0, timestamp = 100),
            makeLog(supplementId = 2, date = "2025-01-01", fraction = 0.5, timestamp = 200)
        )

        dao.insertDoseLogs(logs)

        val all = dao.getAllDoseLogs()
        Assert.assertEquals(2, all.size)
    }

    @Test
    fun insertAll_nonSuspendAlsoWorks() = runTest {
        val logs = listOf(
            makeLog(supplementId = 1, date = "2025-01-01", fraction = 1.0, timestamp = 100),
            makeLog(supplementId = 1, date = "2025-01-01", fraction = 2.0, timestamp = 200)
        )

        dao.insertAll(logs)

        val all = dao.getAllDoseLogs()
        Assert.assertEquals(2, all.size)
    }

    // -------------------------------------------------------------
    // READ TESTS
    // -------------------------------------------------------------

    @Test
    fun getAllDoseLogs_returnsInserted() = runTest {
        dao.insertDoseLog(makeLog(supplementId = 1, date = "2025-01-01", fraction = 1.0, timestamp = 100))

        val all = dao.getAllDoseLogs()
        Assert.assertEquals(1, all.size)
    }

    @Test
    fun getDoseLogsForDay_flowEmitsCorrectRows() = runTest {
        dao.insertDoseLogs(
            listOf(
                makeLog(supplementId = 1, date = "2025-02-01", fraction = 1.0, timestamp = 10),
                makeLog(supplementId = 2, date = "2025-02-01", fraction = 2.0, timestamp = 20),
                makeLog(supplementId = 3, date = "2025-02-02", fraction = 1.0, timestamp = 30)
            )
        )

        val logs = dao.getDoseLogsForDay("2025-02-01").first()
        Assert.assertEquals(2, logs.size)
        Assert.assertTrue(logs.all { it.date == "2025-02-01" })
    }

    @Test
    fun getDoseLogsForDayOnce_returnsCorrectList() = runTest {
        dao.insertDoseLog(makeLog(supplementId = 99, date = "2025-03-01", fraction = 1.0, timestamp = 100))

        val logs = dao.getDoseLogsForDayOnce("2025-03-01")
        Assert.assertEquals(1, logs.size)
    }

    @Test
    fun getLogsForSupplement_returnsCorrectRows() = runTest {
        dao.insertDoseLogs(
            listOf(
                makeLog(supplementId = 1, date = "2025-04-01", fraction = 1.0, timestamp = 100),
                makeLog(supplementId = 1, date = "2025-04-02", fraction = 1.5, timestamp = 200),
                makeLog(supplementId = 2, date = "2025-04-01", fraction = 2.0, timestamp = 150)
            )
        )

        val logs = dao.getLogsForSupplement(1).first()
        Assert.assertEquals(2, logs.size)
        Assert.assertTrue(logs.all { it.supplementId == 1L })
    }

    @Test
    fun getDoseLogById_findsCorrectRecord() = runTest {
        val id = dao.insertDoseLog(makeLog(supplementId = 1, date = "2025-05-01", fraction = 2.0, timestamp = 300))

        val loaded = dao.getDoseLogById(id)
        Assert.assertNotNull(loaded)
        Assert.assertEquals(2.0, loaded!!.actualServingTaken, 0.0)
    }

    // -------------------------------------------------------------
    // DELETE TESTS
    // -------------------------------------------------------------

    @Test
    fun deleteDoseLog_removesEntry() = runTest {
        val id = dao.insertDoseLog(makeLog(supplementId = 1, date = "2025-06-01", fraction = 1.0, timestamp = 100))

        val entity = dao.getDoseLogById(id)!!
        dao.deleteDoseLog(entity)

        Assert.assertNull(dao.getDoseLogById(id))
    }

    @Test
    fun deleteLogsBefore_removesOldRows() = runTest {
        dao.insertDoseLogs(
            listOf(
                makeLog(supplementId = 1, date = "2025-01-01", fraction = 1.0, timestamp = 10),
                makeLog(supplementId = 1, date = "2025-02-01", fraction = 1.0, timestamp = 20),
                makeLog(supplementId = 1, date = "2025-03-01", fraction = 1.0, timestamp = 30)
            )
        )

        dao.deleteLogsBefore("2025-02-01")

        val remaining = dao.getAllDoseLogs()
        Assert.assertEquals(2, remaining.size)
        Assert.assertTrue(remaining.none { it.date == "2025-01-01" })
    }
}
