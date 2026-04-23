package com.tyrads.sdk.acmo.helpers

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for [UrlLauncher].
 * Runs on device so Android's Intent and Uri are natively available.
 */
@RunWith(AndroidJUnit4::class)
class UrlLauncherInstrumentedTest {

    @Test
    fun launchUrlForce_createsAndStartsIntentWithCorrectFlags() {
        val mockContext = mockk<Context>(relaxed = true)
        val testUrl = "https://tyrads.com"

        launchUrlForce(mockContext, testUrl)

        val intentSlot = slot<Intent>()
        verify(exactly = 1) { mockContext.startActivity(capture(intentSlot)) }

        val capturedIntent = intentSlot.captured
        assertEquals(Intent.ACTION_VIEW, capturedIntent.action)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, capturedIntent.flags)
        assertEquals(testUrl, capturedIntent.data?.toString())
    }
}
