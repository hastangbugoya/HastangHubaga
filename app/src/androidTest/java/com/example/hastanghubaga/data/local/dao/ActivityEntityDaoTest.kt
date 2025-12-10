package com.example.hastanghubaga.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import javax.inject.Inject
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.google.common.truth.Truth.assertThat
import org.junit.runner.RunWith

@HiltAndroidTest
@SmallTest
@RunWith(AndroidJUnit4::class)
class ActivityEntityDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase

    private lateinit var dao: ActivityEntityDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = db.activityEntityDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertActivity_andRetrieve_it() = runTest {
        val activity = ActivityEntity(
            type = ActivityType.STRENGTH_TRAINING,
            startTimestamp = 1000L,
            endTimestamp = 2000L
        )

        val id = dao.insertActivity(activity)

        val result = dao.observeActivity(id).first()

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(id)
        assertThat(result?.type).isEqualTo(ActivityType.STRENGTH_TRAINING)
        assertThat(result?.startTimestamp).isEqualTo(1000L)
        assertThat(result?.endTimestamp).isEqualTo(2000L)
    }

    @Test
    fun observeAllActivities_returnsCorrectOrder() = runTest {
        val a1 = ActivityEntity(type = ActivityType.WALKING, startTimestamp = 500L, endTimestamp = 600L)
        val a2 = ActivityEntity(type = ActivityType.RUNNING, startTimestamp = 1500L, endTimestamp = 1600L)

        dao.insertActivity(a1)
        dao.insertActivity(a2)

        val list = dao.observeAllActivities().first()

        // should be ordered DESC by startTimestamp
        assertThat(list.first().type).isEqualTo(ActivityType.RUNNING)
        assertThat(list.last().type).isEqualTo(ActivityType.WALKING)
    }

    @Test
    fun deleteActivity_removes_it() = runTest {
        val activity = ActivityEntity(
            type = ActivityType.YOGA,
            startTimestamp = 200,
            endTimestamp = 400
        )

        val id = dao.insertActivity(activity)
        val stored = dao.observeActivity(id).first()
        assertThat(stored).isNotNull()

        dao.deleteActivity(stored!!)

        val afterDelete = dao.observeActivity(id).first()
        assertThat(afterDelete).isNull()
    }

    @Test
    fun clearAll_clearsTable() = runTest {
        dao.insertActivity(ActivityEntity(1, ActivityType.WALKING,  startTimestamp = 500L, endTimestamp = 600L))
        dao.insertActivity(ActivityEntity(2, ActivityType.RUNNING,  startTimestamp = 500L, endTimestamp = 600L))

        var list = dao.observeAllActivities().first()
        assertThat(list.size).isEqualTo(2)

        dao.clearAll()

        list = dao.observeAllActivities().first()
        assertThat(list).isEmpty()
    }
}
