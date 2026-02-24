package com.tyrads.sdk.acmo.modules.notifications

import androidx.annotation.Keep
import com.tyrads.sdk.acmo.core.utils.SecurityUtils


@Keep
object FirebaseConfig {
    private val P_ID = byteArrayOf(71, 75, 71, 65, 0, 8, 72, 3, 86, 6, 4, 92)
    private val A_ID = byteArrayOf(2, 8, 6, 4, 81, 87, 86, 9, 87, 80, 3, 12, 15, 91, 2, 86, 93, 86, 71, 93, 13, 7, 95, 81, 5, 4, 1, 11, 3, 80, 92, 84, 86, 2, 81, 87, 86, 90, 83, 82, 92, 82, 84, 93, 87)
    private val K_ID = byteArrayOf(114, 123, 79, 83, 55, 26, 36, 95, 61, 6, 94, 82, 111, 16, 114, 15, 3, 99, 71, 68, 54, 15, 19, 64, 39, 33, 115, 115, 101, 4, 11, 111, 71, 101, 69, 64, 20, 85, 85)
    private val S_ID = byteArrayOf(0, 4, 0, 6, 87, 90, 86, 5, 85, 80, 11, 0)
    private val B_ID = byteArrayOf(71, 75, 71, 65, 0, 8, 72, 3, 86, 6, 4, 92, 24, 4, 81, 69, 86, 80, 84, 65, 1, 16, 17, 95, 22, 4, 85, 92, 24, 3, 72, 71)

    val PROJECT_ID: String get() = SecurityUtils.deobfuscate(P_ID)
    val APP_ID_ANDROID: String get() = SecurityUtils.deobfuscate(A_ID)
    val API_KEY_ANDROID: String get() = SecurityUtils.deobfuscate(K_ID)
    val MESSAGING_SENDER_ID: String get() = SecurityUtils.deobfuscate(S_ID)
    val STORAGE_BUCKET: String get() = SecurityUtils.deobfuscate(B_ID)

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