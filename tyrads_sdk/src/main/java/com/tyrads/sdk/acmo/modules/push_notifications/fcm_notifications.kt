package com.tyrads.sdk.acmo.modules.push_notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tyrads.sdk.R
import com.tyrads.sdk.acmo.modules.push_notifications.activity.NotificationPermissionActivity
import android.app.PendingIntent
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import android.widget.Toast
import android.content.BroadcastReceiver
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson

@Keep
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        val dataString = intent.getStringExtra(FCMNotifications.EXTRA_NOTIFICATION_DATA)

        when (action) {
            FCMNotifications.ACTION_NOTIFICATION_CLICKED -> {
                FCMNotifications.getInstance().onNotificationClicked(dataString ?: "{}")
            }

            FCMNotifications.ACTION_NOTIFICATION_DISMISSED -> {
                FCMNotifications.getInstance().onNotificationDismissed(dataString ?: "{}")
            }
        }
    }
}

@Keep
class FCMNotifications private constructor() {

    companion object {
        private const val CHANNEL_ID = "tyrads_channel"
        private const val CHANNEL_NAME = "TyrAds Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications from TyrAds SDK"
        private const val TAG = "FCMNotifications"

        const val ACTION_NOTIFICATION_CLICKED = "com.tyrads.sdk.NOTIFICATION_CLICKED"
        const val ACTION_NOTIFICATION_DISMISSED = "com.tyrads.sdk.NOTIFICATION_DISMISSED"
        const val EXTRA_NOTIFICATION_DATA = "notification_data"

        @Volatile
        private var instance: FCMNotifications? = null

        fun getInstance(): FCMNotifications {
            return instance ?: synchronized(this) {
                instance ?: FCMNotifications().also { instance = it }
            }
        }
    }

    fun initialize(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    suspend fun showNotification(
        context: Context,
        title: String,
        body: String,
        imageUrl: String? = null,
        payload: Map<String, String>? = null
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "showNotification called - Title: $title, Body: $body, ImageUrl: $imageUrl")

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted, requesting...")
                requestNotificationPermission(context)
                return@withContext
            }
        }

        // Check if notifications are enabled
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.e(TAG, "Notifications are disabled by user!")
            return@withContext
        }

        val notificationId = System.currentTimeMillis().toInt()
        Log.d(TAG, "Creating notification with ID: $notificationId")

        val gson = Gson()
        val dataString = if (payload != null) gson.toJson(payload) else "{}"

        val clickIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_NOTIFICATION_CLICKED
            putExtra(EXTRA_NOTIFICATION_DATA, dataString)
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_NOTIFICATION_DISMISSED
            putExtra(EXTRA_NOTIFICATION_DATA, dataString)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title).setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(clickPendingIntent)
                .setDeleteIntent(dismissPendingIntent)

        // Handle image if provided
        if (!imageUrl.isNullOrBlank()) {
            try {
                Log.d(TAG, "Downloading image: $imageUrl")
                val bitmap = downloadImage(context, imageUrl, notificationId)
                if (bitmap != null) {
                    Log.d(TAG, "Image downloaded successfully")
                    builder.setLargeIcon(bitmap)
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                            .bigLargeIcon(null as Bitmap?)
                    )
                } else {
                    Log.w(TAG, "Image download failed, using big text style")
                    builder.setStyle(
                        NotificationCompat.BigTextStyle().bigText(body)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load notification image: ${e.message}", e)
                builder.setStyle(
                    NotificationCompat.BigTextStyle().bigText(body)
                )
            }
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle().bigText(body)
            )
        }

        // Show notification on main thread
        withContext(Dispatchers.Main) {
            try {
                Log.d(TAG, "Displaying notification...")
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationId, builder.build())
                }
                Log.d(TAG, "Notification displayed successfully with ID: $notificationId")
            } catch (e: SecurityException) {
                Log.e(TAG, "Missing notification permission: ${e.message}", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error displaying notification: ${e.message}", e)
            }
        }
    }

    private suspend fun downloadImage(
        context: Context, imageUrl: String, notificationId: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "TyrAds-Android-SDK")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.doInput = true
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(
                    TAG, "Image download failed with response code: ${connection.responseCode}"
                )
                return@withContext null
            }

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

            if (bitmap != null) {
                // Cache the image
                cacheImage(context, bitmap, notificationId)
            }

            inputStream.close()
            connection.disconnect()

            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download notification image: ${e.message}", e)
            null
        }
    }

    private fun cacheImage(context: Context, bitmap: Bitmap, notificationId: Int) {
        try {
            val cacheDir = context.cacheDir
            val imageFile = File(cacheDir, "notification_image_$notificationId.jpg")

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Log.d(TAG, "Image cached successfully: ${imageFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache notification image: ${e.message}", e)
        }
    }

    fun clearCachedImages(context: Context) {
        try {
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("notification_image_")) {
                    file.delete()
                }
            }
            Log.d(TAG, "Cached images cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cached images: ${e.message}", e)
        }
    }

    private fun requestNotificationPermission(context: Context) {
        try {
            val intent = Intent(context, NotificationPermissionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "Launched permission request activity")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request notification permission: ${e.message}", e)
        }
    }

    // ✅ LISTENER 2: onClick - Called when user clicks notification
    internal fun onNotificationClicked(dataString: String) {
        Log.i(TAG, "Notification Clicked Event")
        Log.i(TAG, "Click Data String: $dataString")

        val data = try {
            val gson = Gson()
            gson.fromJson(dataString, Map::class.java) as Map<String, String>
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse data as JSON: ${e.message}")
            mapOf("deepLink" to dataString)
        }
        
        handleNotificationEvent("onClick", data)
    }

    internal fun handleNotificationEvent(eventType: String, data: Map<String, String>) {
        val tyrads = Tyrads.getInstance()
        val deepLinkRoute = data["deepLink"]
        
        try {
            if (!deepLinkRoute.isNullOrEmpty() && eventType == "onClick") {
                tyrads.tyradScope.launch {
                    if (!tyrads.isLoggedIn.value) {
                        try {
                            withTimeout(15000) {
                                tyrads.isLoggedIn.first { it }
                            }
                        } catch (e: Exception) {
                            tyrads.log("Timeout waiting for SDK initialization: ${e.message}", Log.ERROR)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(tyrads.context, "Initialization timed out. Please try again.", Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }
                    }
                    tyrads.showOffers(route = deepLinkRoute)
                }
            }
        } catch (e: Exception) {
            tyrads.log("Could not process notification event $eventType: ${e.message}", Log.ERROR)
        }
    }

    fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return

        val dataString = intent.getStringExtra(EXTRA_NOTIFICATION_DATA)
        if (dataString != null) {
            onNotificationClicked(dataString)
            intent.removeExtra(EXTRA_NOTIFICATION_DATA)
            return
        }

        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("google.message_id") || extras.containsKey("from")) {
                val dataMap = mutableMapOf<String, String>()
                extras.keySet().forEach { key ->
                    extras.get(key)?.let { value ->
                        dataMap[key] = value.toString()
                    }
                }
                handleNotificationEvent("onClick", dataMap)
                intent.removeExtra("google.message_id")
                intent.removeExtra("from")
            } else {
                Tyrads.getInstance().log("No FCM identifying keys found in extras", Log.INFO)
            }
        } else {
            Tyrads.getInstance().log("No extras found in intent", Log.INFO)
        }
    }

    // ✅ LISTENER 3: onDismiss - Called when user dismisses notification
    internal fun onNotificationDismissed(dataString: String) {
        handleNotificationEvent("onDismiss", mapOf("data" to dataString))
    }

    suspend fun showTextNotification(
        context: Context, title: String, body: String, payload: Map<String, String>? = null
    ) {
        showNotification(context, title, body, null, payload)
    }

    suspend fun showImageNotification(
        context: Context,
        title: String,
        body: String,
        imageUrl: String,
        payload: Map<String, String>? = null
    ) {
        showNotification(context, title, body, imageUrl, payload)
    }
}