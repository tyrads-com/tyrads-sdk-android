package com.tyrads.sdk.acmo.modules.notifications

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
import com.tyrads.sdk.acmo.modules.notifications.activity.NotificationPermissionActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Keep
class FCMNotifications private constructor() {

    companion object {
        private const val CHANNEL_ID = "tyrads_channel"
        private const val CHANNEL_NAME = "TyrAds Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications from TyrAds SDK"
        private const val TAG = "FCMNotifications"

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

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Handle image if provided
        if (!imageUrl.isNullOrBlank()) {
            try {
                Log.d(TAG, "Downloading image: $imageUrl")
                val bitmap = downloadImage(context, imageUrl, notificationId)
                if (bitmap != null) {
                    Log.d(TAG, "Image downloaded successfully")
                    builder.setLargeIcon(bitmap)
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null as Bitmap?)
                    )
                } else {
                    Log.w(TAG, "Image download failed, using big text style")
                    builder.setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(body)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load notification image: ${e.message}", e)
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
            }
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
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

    private suspend fun downloadImage(context: Context, imageUrl: String, notificationId: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "TyrAds-Android-SDK")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doInput = true
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Image download failed with response code: ${connection.responseCode}")
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

    suspend fun showTextNotification(
        context: Context,
        title: String,
        body: String,
        payload: Map<String, String>? = null
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