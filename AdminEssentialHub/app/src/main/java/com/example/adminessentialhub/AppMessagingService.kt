package com.example.adminessentialhub

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val CHANNEL_ID = "orders"

class AppMessagingService : FirebaseMessagingService() {

    // Token badla (naya phone, app data clear, waghera)
    override fun onNewToken(token: String) {
        FcmTokenManager.saveToken(token)
    }

    // Cloud Function sirf "data" bhejta hai, isliye ye app khuli ho ya band, hamesha chalta hai
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"] ?: return
        val body = message.data["body"].orEmpty()
        val orderId = message.data["orderId"] ?: title
        // Ek order ke baar-baar status badle to naya notification purane ko replace kare
        showOrderNotification(applicationContext, title, body, orderId.hashCode())
    }
}

fun showOrderNotification(context: Context, title: String, body: String, notificationId: Int) {
    // Android 13+: permission nahi di to chup-chap chhod do
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) return

    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(CHANNEL_ID, "Orders", NotificationManager.IMPORTANCE_HIGH)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Tap karne par app khule
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val pendingIntent = PendingIntent.getActivity(
        context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        // TODO: apna chhota icon (res/drawable/ic_notification) lagao, ye sirf placeholder hai
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    NotificationManagerCompat.from(context).notify(notificationId, notification)
}