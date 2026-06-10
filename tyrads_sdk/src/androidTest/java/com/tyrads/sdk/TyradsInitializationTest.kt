package com.tyrads.sdk

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [Tyrads] singleton initialization behavior.
 * These tests run on a device/emulator where a real Application Context is available.
 */
@RunWith(AndroidJUnit4::class)
class TyradsInitializationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    // ---- Singleton ----

    @Test
    fun getInstance_returnsSameReference_always() {
        val instance1 = Tyrads.getInstance()
        val instance2 = Tyrads.getInstance()
        assertSame(
            "Tyrads.getInstance() must always return the same singleton object",
            instance1,
            instance2
        )
    }

    @Test
    fun getInstance_isNotNull() {
        assertNotNull(Tyrads.getInstance())
    }

    // ---- Init validation ----

    @Test
    fun init_blankApiKey_throwsIllegalArgumentException() {
        var caughtException: Exception? = null
        Tyrads.getInstance().init(
            context = context,
            apiKey = "   ",
            apiSecret = "valid-secret",
            callback = object : TyradsCallback {
                override fun onSuccess() {}
                override fun onFailure(error: String) {
                    caughtException = IllegalArgumentException(error)
                }
            }
        )

        // Give the coroutine time to execute
        Thread.sleep(500)

        assertNotNull(
            "Blank API key should trigger onFailure",
            caughtException
        )
    }

    @Test
    fun init_emptyApiKey_triggersOnFailure() {
        var failureCalled = false
        Tyrads.getInstance().init(
            context = context,
            apiKey = "",
            apiSecret = "valid-secret",
            callback = object : TyradsCallback {
                override fun onSuccess() {}
                override fun onFailure(error: String) {
                    failureCalled = true
                }
            }
        )

        Thread.sleep(500)
        assertTrue("Empty API key should trigger onFailure", failureCalled)
    }

    @Test
    fun init_blankApiSecret_triggersOnFailure() {
        var failureCalled = false
        Tyrads.getInstance().init(
            context = context,
            apiKey = "valid-key",
            apiSecret = "",
            callback = object : TyradsCallback {
                override fun onSuccess() {}
                override fun onFailure(error: String) {
                    failureCalled = true
                }
            }
        )

        Thread.sleep(500)
        assertTrue("Empty API secret should trigger onFailure", failureCalled)
    }

    // ---- Logging ----

    @Test
    fun log_withDebugModeFalse_doesNotCrash() {
        val tyrads = Tyrads.getInstance()
        // Should not throw even if context is not fully initialized
        try {
            tyrads.log("Test message")
        } catch (e: Exception) {
            fail("log() should not throw any exception: ${e.message}")
        }
    }

    // ---- Config ----

    @Test
    fun setMediaSourceInfo_doesNotCrash() {
        val tyrads = Tyrads.getInstance()
        try {
            tyrads.setMediaSourceInfo(TyradsMediaSourceInfo())
        } catch (e: Exception) {
            fail("setMediaSourceInfo() should not throw: ${e.message}")
        }
    }

    @Test
    fun setUserInfo_doesNotCrash() {
        val tyrads = Tyrads.getInstance()
        try {
            tyrads.setUserInfo(TyradsUserInfo(email = "test@test.com"))
        } catch (e: Exception) {
            fail("setUserInfo() should not throw: ${e.message}")
        }
    }

    // ---- Newly Added Coverage ----

    @Test
    fun getWebUri_constructsValidUri() {
        val tyrads = Tyrads.getInstance()
        tyrads.token = "test-token"
        
        val uriStr = tyrads.getWebUri("offerwall", 101)
        val uri = android.net.Uri.parse(uriStr)
        assertEquals("offerwall/101", uri.getQueryParameter("to"))
        assertEquals("test-token", uri.getQueryParameter("token"))
    }

    @Test
    fun track_executesWithoutCrashing() {
        val tyrads = Tyrads.getInstance()
        try {
            tyrads.track("test_instrumented_event")
        } catch (e: Exception) {
            fail("track() should not throw: ${e.message}")
        }
    }

    @Test
    fun changeLanguage_executesWithoutCrashing() = kotlinx.coroutines.runBlocking {
        val tyrads = Tyrads.getInstance()
        try {
            tyrads.changeLanguage("es")
            assertEquals("es", tyrads.currentLanguageCode.value)
        } catch (e: Exception) {
            fail("changeLanguage() should not throw: ${e.message}")
        }
    }
}
