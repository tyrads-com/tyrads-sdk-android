package com.tyrads.sdk.acmo.core.utils

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for VpnInfo utility.
 * Verifies that the isVpnActive function can safely execute on a real Android Context
 * without crashing, regardless of whether a VPN is actually connected on the emulator/device.
 */
@RunWith(AndroidJUnit4::class)
class VpnInfoInstrumentedTest {

    @Test
    fun isVpnActive_executesWithoutCrashing() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // We cannot reliably assert true or false because the environment might have a VPN or not.
        // The key is that this Android system service call executes safely.
        val isActive = isVpnActive(context)
        
        assertNotNull("isVpnActive should return a boolean", isActive)
    }
}
