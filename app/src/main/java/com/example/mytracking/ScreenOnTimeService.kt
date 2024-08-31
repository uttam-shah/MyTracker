package com.example.mytracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mytracking.viewmodel.MainViewModel

class ScreenOnTimeService : Service() {

    private val CHANNEL_ID = "ScreenOnTimeChannel"
    private lateinit var viewModel: MainViewModel
    private lateinit var screenOnOffReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        viewModel = MainViewModel(application)

        createNotificationChannel()
        startForeground(1, createNotification())
        registerScreenOnOffReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle any additional setup if needed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOnOffReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen On Time Tracker")
            .setContentText("Tracking screen-on time...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Screen On Time Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun registerScreenOnOffReceiver() {
        screenOnOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        viewModel.resetLastScreenOnTime()
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        viewModel.updateScreenOnTime()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenOnOffReceiver, filter)
    }
}
