package com.tyrads.sdk.acmo.modules.device_details

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceDetailsInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        // Initialize Tyrads minimally because AcmoDeviceDetailsController relies on Tyrads.getInstance().context
        Tyrads.getInstance().init(
            context = context,
            apiKey = "test-api-key",
            apiSecret = "test-api-secret"
        )
    }

    @Test
    fun getDeviceDetails_returnsMapWithRequiredKeys() = runBlocking {
        val controller = AcmoDeviceDetailsController()
        val details = controller.getDeviceDetails()

        assertNotNull("Device details should not be null", details)
        assertTrue("Device details should not be empty", details.isNotEmpty())

        // Check for Android ID and package info
        assertNotNull(details["androidId"])
        assertEquals(context.packageName, details["package"])
        
        // Check for OS metrics
        assertNotNull(details["brand"])
        assertNotNull(details["model"])
        assertNotNull(details["sdkVersion"])
        assertNotNull(details["rooted"])
        assertNotNull(details["virtual"])
        
        // Ensure tracking metrics are present (even if null based on permission/device)
        assertTrue(details.containsKey("screenDensity"))
        assertTrue(details.containsKey("screenWidth"))
        assertTrue(details.containsKey("screenHeight"))
        assertTrue(details.containsKey("freeMemory"))
    }
}
