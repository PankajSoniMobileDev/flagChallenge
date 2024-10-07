package com.appadore.flagchallenge

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.WorkManagerTestInitHelper
import com.appadore.flagchallenge.util.ChallengeWorker
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class WorkManagerTest {

    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        // Set up WorkManager for testing
        val context = Mockito.mock(Context::class.java)
        val config = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.DEBUG).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun `test schedule challenge with WorkManager`() {
        // Create input data for the worker
        val inputData = Data.Builder().putLong("startTime", System.currentTimeMillis())
            .putInt("totalSecondsScheduled", 3600) // 1 hour
            .build()

        val request = OneTimeWorkRequestBuilder<ChallengeWorker>()
            .setInputData(inputData)
            .build()

        // Enqueue the request
        workManager.enqueue(request)

        // Verify the request is in WorkManager
        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
    }
}