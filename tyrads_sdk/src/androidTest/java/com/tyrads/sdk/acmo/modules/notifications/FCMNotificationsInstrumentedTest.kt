package com.tyrads.sdk.acmo.modules.notifications

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tyrads.sdk.Tyrads
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class FCMNotificationsInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        Tyrads.getInstance().context = context
    }

    @Test
    fun handleNotificationIntent_withNullIntent_doesNotCrash() {
        val fcmNotifications = FCMNotifications.getInstance()
        try {
            fcmNotifications.handleNotificationIntent(null)
            assertTrue("Handled null intent without crashing", true)
        } catch (e: Exception) {
            fail("Should not crash on null intent: ${e.message}")
        }
    }

    @Test
    fun handleNotificationIntent_withEmptyIntent_doesNotCrash() {
        val fcmNotifications = FCMNotifications.getInstance()
        val intent = Intent()
        try {
            fcmNotifications.handleNotificationIntent(intent)
            assertTrue("Handled empty intent without crashing", true)
        } catch (e: Exception) {
            fail("Should not crash on empty intent: ${e.message}")
        }
    }

    @Test
    fun handleNotificationIntent_withCampaignId_processesSuccessfully() {
        val fcmNotifications = FCMNotifications.getInstance()
        val intent = Intent().apply {
            putExtra("campaign_id", "12345")
            putExtra("route", "offerwall")
        }
        try {
            fcmNotifications.handleNotificationIntent(intent)
            assertTrue("Handled valid intent without crashing", true)
        } catch (e: Exception) {
            fail("Should not crash on valid intent: ${e.message}")
        }
    }
}
