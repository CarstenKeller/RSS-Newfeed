package com.carstenkeller.rssnewfeed.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.carstenkeller.rssnewfeed.R

private const val CHANNEL_ID = "new_articles"
private const val NOTIFICATION_ID = 1001

object NewArticlesNotifier {

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Neue Artikel zu beobachteten Themen",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    /** Shows one summary notification for newly fetched articles matching a watched topic. */
    fun notify(context: Context, matchCount: Int, exampleTitle: String) {
        if (matchCount <= 0) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentText = if (matchCount == 1) {
            exampleTitle
        } else {
            "$exampleTitle (+ ${matchCount - 1} weitere)"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Neue Artikel zu deinen Themen")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
