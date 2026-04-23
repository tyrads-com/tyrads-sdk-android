package com.tyrads.sdk.acmo.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tyrads.sdk.acmo.helpers.AcmoEncrypt
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [AcmoEncrypt] — uses real android.util.Base64
 * so must run on a device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class AcmoEncryptInstrumentedTest {

    // A valid 32-byte key for AES-256
    private val validKey32 = "12345678901234567890123456789012" // exactly 32 bytes

    /**
     * Stub Tyrads.isSecure = true by creating AcmoEncrypt directly
     * (the key validation inside AcmoEncrypt checks isSecure from Tyrads.getInstance()).
     * We skip that check here by providing a properly-sized key directly.
     */

    @Test
    fun encryptAndDecrypt_roundTrip_returnsOriginalData() {
        // Create a non-secure encrypt instance (skips key length validation)
        // We bypass `Tyrads.isSecure` by not initializing Tyrads.
        // AcmoEncrypt only enforces key length when isSecure==true.
        val encryptor = AcmoEncrypt(encryptionKey = validKey32)

        val originalData = mapOf(
            "userId" to "user123",
            "platform" to "Android",
            "score" to 42
        )

        val encrypted = encryptor.encryptDataAESGCM(originalData)

        // Encrypted output should contain val, vec, tag
        assertNotNull(encrypted["val"])
        assertNotNull(encrypted["vec"])
        assertNotNull(encrypted["tag"])
        assertTrue(encrypted["val"]!!.isNotEmpty())
        assertTrue(encrypted["vec"]!!.isNotEmpty())
        assertTrue(encrypted["tag"]!!.isNotEmpty())

        val decrypted = encryptor.decryptDataAESGCM(encrypted)
        assertNotNull(decrypted)
        assertTrue(decrypted.isNotEmpty())

        // Verify the decrypted JSON contains the original values
        assertTrue(decrypted.contains("user123"))
        assertTrue(decrypted.contains("Android"))
        assertTrue(decrypted.contains("42"))
    }

    @Test
    fun encrypt_emptyMap_doesNotCrash() {
        val encryptor = AcmoEncrypt(encryptionKey = validKey32)
        val encrypted = encryptor.encryptDataAESGCM(emptyMap())
        // Should not throw and should return some output
        assertNotNull(encrypted)
    }

    @Test
    fun decrypt_emptyEncryptedData_returnsEmptyString() {
        val encryptor = AcmoEncrypt(encryptionKey = validKey32)
        val result = encryptor.decryptDataAESGCM(mapOf("val" to "", "vec" to "", "tag" to ""))
        // Should return empty string without crashing
        assertEquals("", result)
    }

    @Test
    fun encrypt_withStringValues_producesNonEmptyOutput() {
        val encryptor = AcmoEncrypt(encryptionKey = validKey32)
        val data = mapOf<String, Any?>(
            "name" to "John Doe",
            "email" to "john@example.com"
        )
        val encrypted = encryptor.encryptDataAESGCM(data)
        assertTrue(encrypted["val"]!!.isNotBlank())
        assertTrue(encrypted["vec"]!!.isNotBlank())
        assertTrue(encrypted["tag"]!!.isNotBlank())
    }

    @Test
    fun encrypt_twoDifferentCalls_produceDifferentIvs() {
        val encryptor = AcmoEncrypt(encryptionKey = validKey32)
        val data = mapOf<String, Any?>("key" to "value")

        val encrypted1 = encryptor.encryptDataAESGCM(data)
        val encrypted2 = encryptor.encryptDataAESGCM(data)

        // IV (vec) should be random each time — different IVs
        assertNotEquals(
            "Each encryption call should use a fresh random IV",
            encrypted1["vec"],
            encrypted2["vec"]
        )
    }

    @Test
    fun decryptWithWrongKey_returnsEmpty() {
        val encryptor1 = AcmoEncrypt(encryptionKey = validKey32)
        val encryptor2 = AcmoEncrypt(encryptionKey = "abcdefghijklmnopqrstuvwxyz123456") // different 32-byte key

        val data = mapOf<String, Any?>("secret" to "data")
        val encrypted = encryptor1.encryptDataAESGCM(data)

        // Decrypting with wrong key — AES-GCM tag verification should fail → returns ""
        val result = encryptor2.decryptDataAESGCM(encrypted)
        assertEquals("", result)
    }
}
