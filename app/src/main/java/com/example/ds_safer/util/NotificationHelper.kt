package com.example.ds_safer.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    private const val CHANNEL_ID = "on_safe_hazard_alert"
    private const val CHANNEL_NAME = "위험 감지 알림"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "온습도 이상 감지 및 VLM 분석 위험 알림"
                enableVibration(true)
                enableLights(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showHazardNotification(
        context: Context,
        eventId: Int?,
        title: String,
        message: String,
        level: String? = null,
    ) {
        // Android 13+ 런타임 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("NOTI", "skipped: POST_NOTIFICATIONS permission not granted event_id=$eventId")
                return
            }
        }
        Log.d("NOTI", "show event_id=$eventId title=$title level=$level")

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 앱 클릭 시 런처 액티비티 실행
        // TODO: extras에 event_id를 담아 알림 상세 화면으로 이동할 수 있도록 확장 가능
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(context, eventId ?: 0, it, pendingFlags)
        }

        val iconRes = try {
            context.resources.getIdentifier(
                "ic_launcher_foreground", "drawable", context.packageName
            ).takeIf { it != 0 } ?: android.R.drawable.ic_dialog_alert
        } catch (_: Exception) {
            android.R.drawable.ic_dialog_alert
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message.take(80))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .apply { pendingIntent?.let { setContentIntent(it) } }
            .build()

        val notifId = (eventId ?: System.currentTimeMillis().toInt()) and Int.MAX_VALUE
        try {
            nm.notify(notifId, notification)
            Log.d("NOTI", "notified notifId=$notifId event_id=$eventId")
        } catch (e: SecurityException) {
            Log.e("NOTI", "failed SecurityException event_id=$eventId error=${e.message}")
        } catch (e: Exception) {
            Log.e("NOTI", "failed event_id=$eventId error=${e.message}")
        }
    }
}
