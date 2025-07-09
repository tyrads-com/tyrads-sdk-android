package com.tyrads.sdk.acmo.helpers

import android.util.Base64
import com.tyrads.sdk.Tyrads
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

class AcmoEncrypt(private val encryptionKey: String) {
    companion object {
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }

    init {
        if (Tyrads.getInstance().isSecure && encryptionKey.toByteArray(StandardCharsets.UTF_8).size != 32) {
            throw IllegalArgumentException("Encryption key must be 32 bytes long.")
        }
    }

    fun encryptDataAESGCM(data: Map<String, Any>): Map<String, String> {
        try {
            val keyBytes = encryptionKey.toByteArray(StandardCharsets.UTF_8)
            val keySpec = SecretKeySpec(keyBytes, "AES")

            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

            val json = JSONObject(data).toString()
            val encryptedBytes = cipher.doFinal(json.toByteArray(StandardCharsets.UTF_8))

            val cipherText = encryptedBytes.copyOfRange(0, encryptedBytes.size - GCM_TAG_LENGTH)
            val tag =
                encryptedBytes.copyOfRange(
                    encryptedBytes.size - GCM_TAG_LENGTH,
                    encryptedBytes.size
                )

            return mapOf(
                "val" to Base64.encodeToString(cipherText, Base64.NO_WRAP),
                "vec" to Base64.encodeToString(iv, Base64.NO_WRAP),
                "tag" to Base64.encodeToString(tag, Base64.NO_WRAP),
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return mapOf("val" to "", "vec" to "", "tag" to "")
        }
    }

    fun decryptDataAESGCM(encryptedData: Map<String, String>): String {
        try {
            val keyBytes = encryptionKey.toByteArray(StandardCharsets.UTF_8)
            val keySpec = SecretKeySpec(keyBytes, "AES")

            val iv = Base64.decode(encryptedData["vec"], Base64.NO_WRAP)
            val cipherText = Base64.decode(encryptedData["val"], Base64.NO_WRAP)
            val tag = Base64.decode(encryptedData["tag"], Base64.NO_WRAP)

            val encryptedBytes = cipherText + tag

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

            val plainBytes = cipher.doFinal(encryptedBytes)
            return String(plainBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}

