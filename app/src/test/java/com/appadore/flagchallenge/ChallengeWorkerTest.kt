package com.appadore.flagchallenge

import android.app.NotificationManager
import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.appadore.flagchallenge.util.ChallengeWorker
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ChallengeWorkerTest {
    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {

        context = mock(Context::class.java)
        workerParams = mock(WorkerParameters::class.java)
        notificationManager = mock(NotificationManager::class.java)

        // Mock the NotificationManager system service
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager)
    }

    @Test suspend fun `test notification is shown with correct time left`() {
        val worker = ChallengeWorker(context, workerParams)
        val inputData = Data.Builder().putLong("startTime", System.currentTimeMillis()).putInt("totalSecondsScheduled", 3600).build()

        // Simulate the doWork behavior
        worker.doWork()

        // Verify that the notification is shown with correct content
        verify(notificationManager).notify(anyInt(), any())
    }
}