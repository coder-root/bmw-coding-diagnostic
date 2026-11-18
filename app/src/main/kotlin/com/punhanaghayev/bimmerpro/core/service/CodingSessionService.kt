package com.punhanaghayev.bimmerpro.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Keeps the Android process and network/BLE/USB socket alive during sensitive ECU coding or service routines.
 */
class CodingSessionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val detail = intent?.getStringExtra(EXTRA_DETAIL) ?: "ECU Kodlama / Diagnostik oturumu aktif"
        ensureChannel()
        val notification = buildNotification(detail)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BMW ECU Kodlama Oturumu",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Kodlama ve diagnostik sırasında bağlantıyı canlı tutar."
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(detail: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BMW Expert PRO Aktif Oturum")
            .setContentText(detail)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()

    companion object {
        private const val CHANNEL_ID = "bimmerpro_coding_session"
        private const val NOTIFICATION_ID = 4201
        private const val ACTION_START = "com.punhanaghayev.bimmerpro.action.START_CODING_SESSION"
        private const val ACTION_STOP = "com.punhanaghayev.bimmerpro.action.STOP_CODING_SESSION"
        private const val EXTRA_DETAIL = "extra_detail"

        fun start(context: Context, detail: String) {
            val intent = Intent(context, CodingSessionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DETAIL, detail)
            }
            runCatching { context.startForegroundService(intent) }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CodingSessionService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
        }
    }
}
