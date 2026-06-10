package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tyrads.sdk.Tyrads
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class InAppNotificationManagerInstrumentedTest {

    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockTyrads: Tyrads

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor

        mockkObject(Tyrads)
        mockTyrads = mockk(relaxed = true)
        every { Tyrads.getInstance() } returns mockTyrads
        every { mockTyrads.preferences } returns mockPrefs
        every { mockTyrads.publisherUserID } returns "test_user_123"

        // Use reflection to inject mockPrefs since object init happens before @Before
        val field = InAppNotificationManager::class.java.getDeclaredField("sharedPreferences")
        field.isAccessible = true
        field.set(InAppNotificationManager, mockPrefs)

        InAppNotificationManager.reset()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    @Test
    fun setLimitedTimeVisible_whenNotShownToday_becomesVisible() {
        every { mockPrefs.getString(any(), any()) } returns null // Not shown today

        InAppNotificationManager.setLimitedTimeVisible(visible = true, hasEvents = true)
        Thread.sleep(100) // allow stateflow combine to emit

        assertEquals(InAppDialogType.LIMITED_TIME, InAppNotificationManager.activeDialog.value)
        verify { mockEditor.putString("isShownInAppNotification_limitedTime_test_user_123", today()) }
    }

    @Test
    fun setLimitedTimeVisible_whenAlreadyShownToday_doesNotBecomeVisible() {
        every { mockPrefs.getString("isShownInAppNotification_limitedTime_test_user_123", any()) } returns today()

        InAppNotificationManager.setLimitedTimeVisible(visible = true, hasEvents = true)
        Thread.sleep(100)

        assertNull(InAppNotificationManager.activeDialog.value)
    }

    @Test
    fun setCurrencySaleVisible_whenNotShownToday_becomesVisible() {
        every { mockPrefs.getString(any(), any()) } returns null

        InAppNotificationManager.setCurrencySaleVisible(visible = true)
        Thread.sleep(100)

        assertEquals(InAppDialogType.CURRENCY_SALE, InAppNotificationManager.activeDialog.value)
        verify { mockEditor.putString("isShownInAppNotification_currencySale_test_user_123", today()) }
    }

    @Test
    fun dismiss_clearsActiveDialog() {
        every { mockPrefs.getString(any(), any()) } returns null
        InAppNotificationManager.setLimitedTimeVisible(visible = true, hasEvents = true)
        Thread.sleep(100)
        // Ensure state is updated before asserting
        assertEquals(InAppDialogType.LIMITED_TIME, InAppNotificationManager.activeDialog.value)

        InAppNotificationManager.dismiss(InAppDialogType.LIMITED_TIME)
        Thread.sleep(100)
        assertNull(InAppNotificationManager.activeDialog.value)
    }

    @Test
    fun resetPreferences_removesKeysForCurrentUser() {
        InAppNotificationManager.resetPreferences()
        verify { mockEditor.remove("isShownInAppNotification_limitedTime_test_user_123") }
        verify { mockEditor.remove("isShownInAppNotification_currencySale_test_user_123") }
        verify { mockEditor.apply() }
    }

    @Test
    fun resetAllUsersPreferences_clearsAllPrefs() {
        InAppNotificationManager.resetAllUsersPreferences()
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }
}
