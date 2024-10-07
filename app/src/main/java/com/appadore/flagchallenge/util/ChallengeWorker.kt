package com.appadore.flagchallenge.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appadore.flagchallenge.R

class ChallengeWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val startTime = inputData.getLong("startTime", 0L)
        val totalSecondsScheduled = inputData.getInt("totalSecondsScheduled", 0)
        val currentTime = System.currentTimeMillis()

        val elapsedTime = (currentTime - startTime) / 1000
        val remainingTime = totalSecondsScheduled - elapsedTime

        return if (remainingTime > 0) {
            showTimerNotification(remainingTime.toInt())
            Result.success()
        } else {
            sendStartChallengeNotification()
            Result.success()
        }
    }

    private fun showTimerNotification(remainingTime: Int) {
        createNotificationChannel()
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, "timer_channel_id")
            .setContentTitle("Challenge Timer")
            .setContentText("Time left: ${remainingTime / 60}:${remainingTime % 60}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun sendStartChallengeNotification() {
        createNotificationChannel()
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, "timer_channel_id")
            .setContentTitle("Challenge Started")
            .setContentText("The challenge is now starting!")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "timer_channel_id"
            val channelName = "Challenge Countdown"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}