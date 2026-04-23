package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels

import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.Campaign
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.LimitedTimeEvent
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.LimitedTimeOfferViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the pure business logic methods in [LimitedTimeOfferViewModel]:
 * - [LimitedTimeOfferViewModel.showCountDown]
 * - [LimitedTimeOfferViewModel.getStatusString]
 * - [LimitedTimeOfferViewModel.onPageChanged]
 *
 * We use StandardTestDispatcher so viewModelScope coroutines don't auto-advance,
 * and we mock Tyrads singleton before ViewModel creation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LimitedTimeOfferViewModelLogicTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LimitedTimeOfferViewModel

    @Before
    fun setUp() {
        // Must set BEFORE ViewModel is created (init block uses viewModelScope → Dispatchers.Main)
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        // Mock Tyrads singleton before ViewModel creation
        mockkObject(Tyrads)
        val mockTyrads = mockk<Tyrads>(relaxed = true)
        every { Tyrads.getInstance() } returns mockTyrads
        every { mockTyrads.currentLanguageCode } returns MutableStateFlow("en")
        every { mockTyrads.log(any(), any(), any()) } just runs

        val mockNetwork = mockk<NetworkCommons>(relaxed = true)
        // The fetch in init block suspends → with StandardTestDispatcher it won't auto-run
        viewModel = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ============================================================
    // showCountDown tests
    // ============================================================

    @Test
    fun showCountDown_nonDuplicate_nullStatus_returnsTrue() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = null)
        assertTrue(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_nonDuplicate_approvedStatus_returnsFalse() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = "approved")
        assertFalse(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_nonDuplicate_pendingStatus_returnsFalse() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = "pending")
        assertFalse(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_nonDuplicate_rejectedStatus_returnsFalse() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = "rejected")
        assertFalse(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_duplicate_dailyCountBelowLimit_returnsTrue() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = 1, dailyLimit = 3)
        assertTrue(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_duplicate_dailyCountEqualsLimit_returnsFalse() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = 3, dailyLimit = 3)
        assertFalse(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_duplicate_dailyCountExceedsLimit_returnsFalse() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = 5, dailyLimit = 3)
        assertFalse(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_duplicate_nullDailyCount_returnsTrue() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = null, dailyLimit = 3)
        assertTrue(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_duplicate_nullDailyLimit_returnsTrue() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = 2, dailyLimit = null)
        assertTrue(viewModel.showCountDown(event))
    }

    @Test
    fun showCountDown_nullAllowDuplicate_treatedAsFalse_nullStatus_returnsTrue() {
        val event = LimitedTimeEvent(allowDuplicateEvents = null, conversionStatus = null)
        assertTrue(viewModel.showCountDown(event))
    }

    // ============================================================
    // getStatusString tests
    // ============================================================

    @Test
    fun getStatusString_nonDuplicate_approvedStatus_returnsCompleted() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = "approved")
        assertEquals("Completed", viewModel.getStatusString(event))
    }

    @Test
    fun getStatusString_nonDuplicate_rejectedStatus_returnsRejected() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = "rejected")
        assertEquals("Rejected", viewModel.getStatusString(event))
    }

    @Test
    fun getStatusString_nonDuplicate_pendingStatus_returnsEmpty() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = "pending")
        assertEquals("", viewModel.getStatusString(event))
    }

    @Test
    fun getStatusString_nonDuplicate_nullStatus_returnsEmpty() {
        val event = LimitedTimeEvent(allowDuplicateEvents = false, conversionStatus = null)
        assertEquals("", viewModel.getStatusString(event))
    }

    @Test
    fun getStatusString_duplicate_dailyLimitComplete_returnsCompleted() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = 3, dailyLimit = 3)
        assertEquals("Completed", viewModel.getStatusString(event))
    }

    @Test
    fun getStatusString_duplicate_dailyLimitNotComplete_returnsEmpty() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = 1, dailyLimit = 3)
        assertEquals("", viewModel.getStatusString(event))
    }

    @Test
    fun getStatusString_duplicate_nullDailyCount_returnsEmpty() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = null, dailyLimit = 3)
        assertEquals("", viewModel.getStatusString(event))
    }

    @Test
    fun getStatusString_duplicate_nullDailyLimit_returnsEmpty() {
        val event = LimitedTimeEvent(allowDuplicateEvents = true, dailyCount = 2, dailyLimit = null)
        assertEquals("", viewModel.getStatusString(event))
    }

    // ============================================================
    // onPageChanged
    // ============================================================

    @Test
    fun onPageChanged_updatesCurrentPageInState() {
        viewModel.onPageChanged(3)
        assertEquals(3, viewModel.uiState.value.currentPage)
    }

    @Test
    fun onPageChanged_multipleUpdates_lastValueWins() {
        viewModel.onPageChanged(1)
        viewModel.onPageChanged(5)
        assertEquals(5, viewModel.uiState.value.currentPage)
    }
}
