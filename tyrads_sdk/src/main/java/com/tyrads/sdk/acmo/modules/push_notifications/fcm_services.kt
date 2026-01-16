package com.tyrads.sdk.acmo.modules.push_notifications

import android.Manifest
import android.content.Context
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
import com.tyrads.sdk.acmo.modules.push_notifications.activity.NotificationPermissionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.core.content.edit

@Keep
class FCMService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        private const val TAG = "TyradsFCMService"

        suspend fun initialize(context: Context) {
            try {
                com.google.firebase.FirebaseApp.initializeApp(
                    context,
                    com.google.firebase.FirebaseOptions.Builder()
                        .setApiKey(FirebaseConfig.API_KEY_ANDROID)
                        .setApplicationId(FirebaseConfig.APP_ID_ANDROID)
                        .setProjectId(FirebaseConfig.PROJECT_ID)
                        .setGcmSenderId(FirebaseConfig.MESSAGING_SENDER_ID)
                        .setStorageBucket(FirebaseConfig.STORAGE_BUCKET).build()
                )
                FCMNotifications.getInstance().initialize(context)
                requestNotificationPermissionIfNeeded(context)
                val token = FirebaseMessaging.getInstance().token.await()
                saveToken(token)
            } catch (e: IllegalStateException) {
                Log.d(TAG, "Firebase already initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FCM: ${e.message}", e)
            }
        }

        private fun requestNotificationPermissionIfNeeded(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
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
                tyrads.preferences.edit {
                    putString(AcmoKeyNames.FCM_TOKEN, token)
                }
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
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")
        FCMNotifications.getInstance().handleNotificationEvent("onReceive", message.data)
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

    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageSent(messageId: String) {
        super.onMessageSent(messageId)
    }

    override fun onSendError(messageId: String, exception: Exception) {
        super.onSendError(messageId, exception)
    }
}