package com.tyrads.sdk.acmo.modules.usage_stats

import AcmoUsageStatsController
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [AcmoUsageStatsController].
 * Validates that interactions with Android AppOpsManager and UsageStatsManager
 * execute safely without crashing.
 */
@RunWith(AndroidJUnit4::class)
class UsageStatsControllerInstrumentedTest {

    private lateinit var context: Context
    private lateinit var controller: AcmoUsageStatsController

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        controller = AcmoUsageStatsController()
    }

    @Test
    fun isUsagePermission_executesSafely() {
        // May return true or false depending on the emulator setup, but should never crash.
        val hasPermission = controller.isUsagePermission(context)
        assertNotNull(hasPermission)
    }

    @Test
    fun checkUsagePermission_executesSafely() {
        // Internal method, should also execute safely
        val hasPermission = controller.checkUsagePermission()
        assertNotNull(hasPermission)
    }
}
