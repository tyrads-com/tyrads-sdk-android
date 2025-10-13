package com.tyrads.sdk.acmo.modules.notifications

import androidx.annotation.Keep

@Keep
object FirebaseConfig {
    const val PROJECT_ID = "tyrsdk-32c6e"
    const val APP_ID_ANDROID = "1:365439351599:android:f4d793b204e29feab87fda"
    const val API_KEY_ANDROID = "AIzaSyCD_nKOf9iDluRLi4El9XU3joaCzlU9zgc"
    const val MESSAGING_SENDER_ID = "365439351599"
    const val STORAGE_BUCKET = "tyrsdk-32c6e.firebasestorage.app"

    fun toMap(): Map<String, String> {
        return mapOf(
            "projectId" to PROJECT_ID,
            "appId" to APP_ID_ANDROID,
            "apiKey" to API_KEY_ANDROID,
            "messagingSenderId" to MESSAGING_SENDER_ID,
            "storageBucket" to STORAGE_BUCKET
        )
    }
}