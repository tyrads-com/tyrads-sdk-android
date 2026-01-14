package com.tyrads.sdk.acmo.modules.notifications

import AcmoKeyNames
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.notifications.activity.NotificationPermissionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Keep
class FCMService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        private const val TAG = "FCMService"

        suspend fun initialize(context: android.content.Context) {
            try {
                Log.d(TAG, "Initializing FCM service...")

                if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                    try {
                        com.google.firebase.FirebaseApp.initializeApp(
                            context,
                            com.google.firebase.FirebaseOptions.Builder()
                                .setApiKey(FirebaseConfig.API_KEY_ANDROID)
                                .setApplicationId(FirebaseConfig.APP_ID_ANDROID)
                                .setProjectId(FirebaseConfig.PROJECT_ID)
                                .setGcmSenderId(FirebaseConfig.MESSAGING_SENDER_ID)
                                .setStorageBucket(FirebaseConfig.STORAGE_BUCKET)
                                .build()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to initialize Firebase: ${e.message}", e)
                        return
                    }
                } else {
                    Log.d(TAG, "Firebase already initialized")
                }

                FCMNotifications.getInstance().initialize(context)

                requestNotificationPermissionIfNeeded(context)

                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM Token: $token")

                saveToken(token)

                Log.d(TAG, "FCM service initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FCM: ${e.message}", e)
            }
        }

        private fun requestNotificationPermissionIfNeeded(context: android.content.Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    Log.d(TAG, "POST_NOTIFICATIONS permission not granted, requesting...")
                    try {
                        val intent = Intent(context, NotificationPermissionActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to request notification permission: ${e.message}", e)
                    }
                } else {
                    Log.d(TAG, "POST_NOTIFICATIONS permission already granted")
                }
            }
        }

        private fun saveToken(token: String) {
            try {
                val tyrads = Tyrads.getInstance()
                tyrads.preferences.edit()
                    .putString(AcmoKeyNames.FCM_TOKEN, token)
                    .apply()
                Log.d(TAG, "FCM token saved: $token")
            } catch (e: UninitializedPropertyAccessException) {
                Log.w(TAG, "Tyrads not initialized yet, FCM token will be saved later")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save FCM token: ${e.message}")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")
        Log.d(TAG, "Message ID: ${message.messageId}")

        // ✅ LISTENER 1: onReceive - Logs when notification is received
        logNotificationEvent("onReceive", message.data)

        serviceScope.launch {
            handleMessage(message)
        }
    }

    private suspend fun handleMessage(message: RemoteMessage) {
        try {
            message.notification?.let { notification ->
                val title = notification.title ?: "TyrAds Notification"
                val body = notification.body ?: ""
                val imageUrl = notification.imageUrl?.toString()

                Log.d(TAG, "Showing notification - Title: $title, Body: $body")

                FCMNotifications.getInstance().showNotification(
                    context = applicationContext,
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    payload = message.data
                )
            }

            if (message.data.isNotEmpty()) {
                handleMessageData(message.data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message: ${e.message}", e)
        }
    }

    private fun handleMessageData(data: Map<String, String>) {
        Log.d(TAG, "Message data: $data")
    }

    private fun logNotificationEvent(eventType: String, data: Map<String, String>) {
        Log.i(TAG, "Notification Event: $eventType")
        Log.i(TAG, "Event Data: $data")

        try {
            val tyrads = Tyrads.getInstance()
            tyrads.log("FCM Notification Event - $eventType: $data", Log.INFO, force = true)
        } catch (e: Exception) {
            Log.w(TAG, "Could not log to Tyrads: ${e.message}")
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "Messages deleted on server")
    }

    override fun onMessageSent(messageId: String) {
        super.onMessageSent(messageId)
        Log.d(TAG, "Message sent: $messageId")
    }

    override fun onSendError(messageId: String, exception: Exception) {
        super.onSendError(messageId, exception)
        Log.e(TAG, "Send error for message $messageId: ${exception.message}")
    }
}