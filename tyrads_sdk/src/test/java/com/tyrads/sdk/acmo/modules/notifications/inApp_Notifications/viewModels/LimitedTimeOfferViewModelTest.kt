package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels

import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.Campaign
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.GroupData
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.LimitedTimeEvent
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.LimitedTimeOfferViewModel
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.LimitedOfferUiState
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
 * Unit tests for [LimitedTimeOfferViewModel]:
 * - [LimitedOfferUiState] default values
 * - [LimitedTimeOfferViewModel.showCountDown]
 * - [LimitedTimeOfferViewModel.getStatusString]
 * - [LimitedTimeOfferViewModel.onPageChanged]
 * - [LimitedTimeOfferViewModel.fetchLimitedEvents] (success / empty / error / filtering)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LimitedTimeOfferViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockNetwork: NetworkCommons
    private lateinit var viewModel: LimitedTimeOfferViewModel

    private fun makeEvent(
        conversionStatus: String? = null,
        allowDuplicateEvents: Boolean? = false,
        dailyCount: Int? = null,
        dailyLimit: Int? = null
    ) = LimitedTimeEvent(
        conversionStatus = conversionStatus,
        allowDuplicateEvents = allowDuplicateEvents,
        dailyCount = dailyCount,
        dailyLimit = dailyLimit
    )

    private fun makeCampaign(
        status: String? = "active",
        isInstalled: Boolean? = true,
        events: List<LimitedTimeEvent>? = listOf(makeEvent())
    ) = Campaign(
        campaignId = 1,
        campaignName = "Test Game",
        status = status,
        isInstalled = isInstalled,
        limitedTimeEvents = events
    )

    private fun makeGroup(
        groupName: String? = "hotdeals",
        campaigns: List<Campaign>? = listOf(makeCampaign())
    ) = GroupData(groupName = groupName, campaigns = campaigns)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        mockNetwork = mockk(relaxed = true)

        mockkObject(Tyrads)
        val mockTyrads = mockk<Tyrads>(relaxed = true)
        every { Tyrads.getInstance() } returns mockTyrads
        every { mockTyrads.currentLanguageCode } returns MutableStateFlow("en")
        every { mockTyrads.log(any(), any(), any()) } just runs

        // Suppress InAppNotificationManager side effects
        mockkObject(com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.InAppNotificationManager)
        every {
            com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.InAppNotificationManager
                .setLimitedTimeVisible(any(), any())
        } just runs

        viewModel = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ============================================================
    // LimitedOfferUiState defaults
    // ============================================================

    @Test
    fun limitedOfferUiState_defaultValues_areCorrect() {
        val state = LimitedOfferUiState()
        assertFalse(state.isLoading)
        assertTrue(state.campaigns.isEmpty())
        assertEquals(0, state.currentPage)
        assertNull(state.error)
    }

    // ============================================================
    // fetchLimitedEvents — async paths
    // ============================================================

    @Test
    fun fetchLimitedEvents_onSuccess_populatesCampaigns() = runTest {
        val groups = listOf(makeGroup())
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns groups

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        // The campaign is installed + active + has events with no approved status → passes filter
        assertEquals(1, state.campaigns.size)
    }

    @Test
    fun fetchLimitedEvents_onNullResponse_campaignsIsEmpty() = runTest {
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns null

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.campaigns.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun fetchLimitedEvents_onException_setsError() = runTest {
        coEvery { mockNetwork.fetchLimitedEvents("en") } throws RuntimeException("Network error")

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.campaigns.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
    }

    @Test
    fun fetchLimitedEvents_filtersOut_nonHotdealsGroups() = runTest {
        val groups = listOf(makeGroup(groupName = "featured"))
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns groups

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        // "featured" group is not "hotdeals" → filtered out
        assertTrue(vm.uiState.value.campaigns.isEmpty())
    }

    @Test
    fun fetchLimitedEvents_filtersOut_suspendedCampaigns() = runTest {
        val campaign = makeCampaign(status = "suspended")
        val groups = listOf(makeGroup(campaigns = listOf(campaign)))
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns groups

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.campaigns.isEmpty())
    }

    @Test
    fun fetchLimitedEvents_filtersOut_notInstalledCampaigns() = runTest {
        val campaign = makeCampaign(isInstalled = false)
        val groups = listOf(makeGroup(campaigns = listOf(campaign)))
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns groups

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.campaigns.isEmpty())
    }

    @Test
    fun fetchLimitedEvents_filtersOut_campaignsWithEmptyEvents() = runTest {
        val campaign = makeCampaign(events = emptyList())
        val groups = listOf(makeGroup(campaigns = listOf(campaign)))
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns groups

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.campaigns.isEmpty())
    }

    @Test
    fun fetchLimitedEvents_filtersOut_allApprovedEvents() = runTest {
        // All events are approved → no non-approved event → filtered out
        val campaign = makeCampaign(
            events = listOf(makeEvent(conversionStatus = "approved"))
        )
        val groups = listOf(makeGroup(campaigns = listOf(campaign)))
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns groups

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.campaigns.isEmpty())
    }

    @Test
    fun fetchLimitedEvents_keeps_campaignWithAtLeastOneNonApprovedEvent() = runTest {
        val campaign = makeCampaign(
            events = listOf(
                makeEvent(conversionStatus = "approved"),
                makeEvent(conversionStatus = null)  // one non-approved → should pass
            )
        )
        val groups = listOf(makeGroup(campaigns = listOf(campaign)))
        coEvery { mockNetwork.fetchLimitedEvents("en") } returns groups

        val vm = LimitedTimeOfferViewModel(networkCommons = mockNetwork)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.campaigns.size)
    }

    // ============================================================
    // onPageChanged
    // ============================================================

    @Test
    fun onPageChanged_updatesCurrentPage() {
        viewModel.onPageChanged(4)
        assertEquals(4, viewModel.uiState.value.currentPage)
    }

    @Test
    fun onPageChanged_multipleUpdates_lastValueWins() {
        viewModel.onPageChanged(1)
        viewModel.onPageChanged(7)
        assertEquals(7, viewModel.uiState.value.currentPage)
    }

    @Test
    fun onPageChanged_zero_setsZero() {
        viewModel.onPageChanged(3)
        viewModel.onPageChanged(0)
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    // ============================================================
    // showCountDown
    // ============================================================

    @Test
    fun showCountDown_nonDuplicate_nullStatus_returnsTrue() {
        assertTrue(viewModel.showCountDown(makeEvent(allowDuplicateEvents = false, conversionStatus = null)))
    }

    @Test
    fun showCountDown_nonDuplicate_approvedStatus_returnsFalse() {
        assertFalse(viewModel.showCountDown(makeEvent(allowDuplicateEvents = false, conversionStatus = "approved")))
    }

    @Test
    fun showCountDown_nonDuplicate_pendingStatus_returnsFalse() {
        assertFalse(viewModel.showCountDown(makeEvent(allowDuplicateEvents = false, conversionStatus = "pending")))
    }

    @Test
    fun showCountDown_nonDuplicate_rejectedStatus_returnsFalse() {
        assertFalse(viewModel.showCountDown(makeEvent(allowDuplicateEvents = false, conversionStatus = "rejected")))
    }

    @Test
    fun showCountDown_duplicate_dailyCountBelowLimit_returnsTrue() {
        assertTrue(viewModel.showCountDown(makeEvent(allowDuplicateEvents = true, dailyCount = 1, dailyLimit = 3)))
    }

    @Test
    fun showCountDown_duplicate_dailyCountEqualsLimit_returnsFalse() {
        assertFalse(viewModel.showCountDown(makeEvent(allowDuplicateEvents = true, dailyCount = 3, dailyLimit = 3)))
    }

    @Test
    fun showCountDown_duplicate_dailyCountExceedsLimit_returnsFalse() {
        assertFalse(viewModel.showCountDown(makeEvent(allowDuplicateEvents = true, dailyCount = 5, dailyLimit = 3)))
    }

    @Test
    fun showCountDown_duplicate_nullDailyCount_returnsTrue() {
        assertTrue(viewModel.showCountDown(makeEvent(allowDuplicateEvents = true, dailyCount = null, dailyLimit = 3)))
    }

    @Test
    fun showCountDown_duplicate_nullDailyLimit_returnsTrue() {
        assertTrue(viewModel.showCountDown(makeEvent(allowDuplicateEvents = true, dailyCount = 2, dailyLimit = null)))
    }

    @Test
    fun showCountDown_nullAllowDuplicate_treatedAsFalse_nullStatus_returnsTrue() {
        assertTrue(viewModel.showCountDown(makeEvent(allowDuplicateEvents = null, conversionStatus = null)))
    }

    // ============================================================
    // getStatusString
    // ============================================================

    @Test
    fun getStatusString_nonDuplicate_approved_returnsCompleted() {
        assertEquals("Completed", viewModel.getStatusString(makeEvent(allowDuplicateEvents = false, conversionStatus = "approved")))
    }

    @Test
    fun getStatusString_nonDuplicate_rejected_returnsRejected() {
        assertEquals("Rejected", viewModel.getStatusString(makeEvent(allowDuplicateEvents = false, conversionStatus = "rejected")))
    }

    @Test
    fun getStatusString_nonDuplicate_pending_returnsEmpty() {
        assertEquals("", viewModel.getStatusString(makeEvent(allowDuplicateEvents = false, conversionStatus = "pending")))
    }

    @Test
    fun getStatusString_nonDuplicate_nullStatus_returnsEmpty() {
        assertEquals("", viewModel.getStatusString(makeEvent(allowDuplicateEvents = false, conversionStatus = null)))
    }

    @Test
    fun getStatusString_duplicate_limitComplete_returnsCompleted() {
        assertEquals("Completed", viewModel.getStatusString(makeEvent(allowDuplicateEvents = true, dailyCount = 3, dailyLimit = 3)))
    }

    @Test
    fun getStatusString_duplicate_limitNotComplete_returnsEmpty() {
        assertEquals("", viewModel.getStatusString(makeEvent(allowDuplicateEvents = true, dailyCount = 1, dailyLimit = 3)))
    }

    @Test
    fun getStatusString_duplicate_nullDailyCount_returnsEmpty() {
        assertEquals("", viewModel.getStatusString(makeEvent(allowDuplicateEvents = true, dailyCount = null, dailyLimit = 3)))
    }

    @Test
    fun getStatusString_duplicate_nullDailyLimit_returnsEmpty() {
        assertEquals("", viewModel.getStatusString(makeEvent(allowDuplicateEvents = true, dailyCount = 2, dailyLimit = null)))
    }
}
