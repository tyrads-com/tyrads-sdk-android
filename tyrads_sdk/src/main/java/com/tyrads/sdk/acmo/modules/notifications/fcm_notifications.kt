package com.tyrads.sdk.acmo.modules.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
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
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.notifications.activity.NotificationPermissionActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Keep
class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotifActionReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        val dataString = intent.getStringExtra(FCMNotifications.EXTRA_NOTIFICATION_DATA)

        when (action) {
            FCMNotifications.ACTION_NOTIFICATION_CLICKED -> {
                FCMNotifications.getInstance().onNotificationClicked(dataString ?: "no_data")
            }

            FCMNotifications.ACTION_NOTIFICATION_DISMISSED -> {
                FCMNotifications.getInstance().onNotificationDismissed(dataString ?: "no_data")
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

    fun jsonToMap(jsonString: String): Map<String, Any> {
        val gson = Gson()
        val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    private fun parseMapStringToJson(mapString: String): com.google.gson.JsonObject {
        val cleanString = mapString.trim().removeSurrounding("{", "}")
        val jsonObject = com.google.gson.JsonObject()

        if (cleanString.isNotEmpty()) {
            // Split by comma, but be careful with nested structures
            cleanString.split(",").forEach { pair ->
                val trimmedPair = pair.trim()
                val equalsIndex = trimmedPair.indexOf('=')
                if (equalsIndex > 0) {
                    val key = trimmedPair.substring(0, equalsIndex).trim()
                    val value = trimmedPair.substring(equalsIndex + 1).trim()
                    jsonObject.addProperty(key, value)
                }
            }
        }

        return jsonObject
    }

    fun initialize(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }
        Log.d(TAG, "FCM Notifications initialized")
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

    internal fun onNotificationClicked(dataString: String) {
        Log.i(TAG, "Notification Clicked Event")
        Log.i(TAG, "Click Data String: $dataString")

        val data = try {
            // Try parsing as JSON first
            JsonParser.parseString(dataString).asJsonObject
        } catch (e: JsonSyntaxException) {
            // Fallback: Handle Kotlin Map toString format like {key=value}
            Log.w(TAG, "Failed to parse as JSON, attempting Map format parsing: ${e.message}")
            try {
                parseMapStringToJson(dataString)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to parse data in any format: ${e2.message}")
                JsonParser.parseString("{}").asJsonObject
            }
        }
        handleNotificationEvent("onClick", data.asMap().mapValues { it.value.toString() })
    }

    private fun com.google.gson.JsonObject.asMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        this.entrySet().forEach { (key, value) ->
            map[key] = value
        }
        return map
    }

    internal fun handleNotificationEvent(eventType: String, data: Map<String, String>) {
        val tyrads = Tyrads.getInstance()

        try {
             val deepLinkRoute = data["deepLink"]
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
            tyrads.log("Could not log to Tyrads: ${e.message}", Log.ERROR)
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
        val tyrads = Tyrads.getInstance()
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
                tyrads.log("No FCM identifying keys (google.message_id or from) found in extras", Log.INFO)
            }
        } else {
            tyrads.log("No extras found in intent", Log.INFO)
        }
    }
    internal fun onNotificationDismissed(dataString: String) {
        handleNotificationEvent("onDismiss", mapOf("data" to dataString))
    }

    suspend fun showNotification(
        context: Context,
        title: String,
        body: String,
        imageUrl: String? = null,
        payload: Map<String, String>? = null
    ) = withContext(Dispatchers.IO) {
        Log.d(TAG, "showNotification called - Title: $title, Body: $body, ImageUrl: $imageUrl")

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

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.e(TAG, "Notifications are disabled by user!")
            return@withContext
        }

        val notificationId = System.currentTimeMillis().toInt()
        Log.d(TAG, "Creating notification with ID: $notificationId")

        // Convert payload to proper JSON string instead of Map.toString()
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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(clickPendingIntent)
            .setDeleteIntent(dismissPendingIntent)

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
        context: Context,
        imageUrl: String,
        notificationId: Int
    ): Bitmap? =
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
                    Log.e(
                        TAG,
                        "Image download failed with response code: ${connection.responseCode}"
                    )
                    return@withContext null
                }

                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap != null) {
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
