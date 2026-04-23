package com.tyrads.sdk.acmo.modules.premium_widgets.view_model

import TyradsActivity
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.*
import com.tyrads.sdk.acmo.modules.premium_widgets.view_model.TopOffersViewModel
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
 * Unit tests for [TopOffersViewModel] using a mocked [NetworkCommons].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TopOffersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockNetworkCommons: NetworkCommons
    private lateinit var mockTyrads: Tyrads

    private fun makeOffer(
        id: Int = 1,
        name: String = "Test Offer",
        premium: Boolean = false,
        totalPlayablePayout: Double = 10.0
    ): AcmoOffersModel = AcmoOffersModel(
        campaignId = id,
        campaignName = name,
        premium = premium,
        app = App(title = name),
        tracking = Tracking(),
        targeting = Targeting(),
        creative = Creative(creativeUrl = "", creativePacks = emptyList()),
        availableCurrencies = mapOf(
            "USD" to AvailableCurrency(currencyId = 1, currencyName = "Coins", currencyIcon = "ic.png")
        ),
        payoutSummary = mapOf(
            "USD" to PayoutSummary(
                totalPayoutConverted = totalPlayablePayout,
                totalPlayablePayoutConverted = totalPlayablePayout
            )
        )
    )

    @Before
    fun setUp() {
        // Must set BEFORE ViewModel creation — init block uses viewModelScope → Dispatchers.Main
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        mockNetworkCommons = mockk(relaxed = true)

        // Mock Tyrads singleton before ViewModel creation
        mockkObject(Tyrads)
        mockTyrads = mockk(relaxed = true)
        every { Tyrads.getInstance() } returns mockTyrads
        every { mockTyrads.currentLanguageCode } returns MutableStateFlow("en")
        every { mockTyrads.log(any(), any(), any()) } just runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun initialState_isLoadingFalse_emptyOffers() {
        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        // Before advancing coroutines, state should be at defaults
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.cachedHotOffers.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun fetchData_onSuccess_populatesOffers() = runTest {
        val offers = listOf(makeOffer(id = 1, name = "App A"), makeOffer(id = 2, name = "App B"))
        val sale = CurrencySales(name = "Double Coins", multiplier = 2.0)

        coEvery { mockNetworkCommons.fetchCampaigns(any()) } returns offers
        coEvery { mockNetworkCommons.fetchCurrencySale(any()) } returns sale
        coEvery { mockNetworkCommons.fetchActiveOffersSummary(any()) } returns 5

        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.cachedHotOffers.size)
        assertEquals(sale, state.currencySales)
        assertEquals(5, state.activeOffersCount)
        assertNull(state.error)
    }

    @Test
    fun fetchData_onNetworkError_populatesErrorField() = runTest {
        coEvery { mockNetworkCommons.fetchCampaigns(any()) } throws RuntimeException("Network timeout")
        coEvery { mockNetworkCommons.fetchCurrencySale(any()) } returns null
        coEvery { mockNetworkCommons.fetchActiveOffersSummary(any()) } returns 0

        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network timeout"))
        assertTrue(state.cachedHotOffers.isEmpty())
    }

    @Test
    fun setLoadingIndex_updatesLoadingIndexInState() = runTest {
        coEvery { mockNetworkCommons.fetchCampaigns(any()) } returns emptyList()
        coEvery { mockNetworkCommons.fetchCurrencySale(any()) } returns null
        coEvery { mockNetworkCommons.fetchActiveOffersSummary(any()) } returns 0

        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        viewModel.setLoadingIndex(2)
        assertEquals(2, viewModel.uiState.value.loadingIndex)

        viewModel.setLoadingIndex(null)
        assertNull(viewModel.uiState.value.loadingIndex)
    }

    @Test
    fun onUrlEventHandled_resetsOpenUrlEvent() = runTest {
        coEvery { mockNetworkCommons.fetchCampaigns(any()) } returns emptyList()
        coEvery { mockNetworkCommons.fetchCurrencySale(any()) } returns null
        coEvery { mockNetworkCommons.fetchActiveOffersSummary(any()) } returns 0

        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        assertNull(viewModel.openUrlEvent.value)
        viewModel.onUrlEventHandled()
        assertNull(viewModel.openUrlEvent.value)
    }

    @Test
    fun topOffersUiState_defaultValues_areCorrect() {
        val state = com.tyrads.sdk.acmo.modules.premium_widgets.view_model.TopOffersUiState()
        assertFalse(state.isLoading)
        assertTrue(state.cachedHotOffers.isEmpty())
        assertNull(state.currencySales)
        assertEquals(0, state.activeOffersCount)
        assertNull(state.error)
        assertNull(state.loadingIndex)
    }

    // ---- onOfferClick Tests ----

    @Test
    fun onOfferClick_whenInstalled_opensPreviewUrl() = runTest {
        val offer = makeOffer(id = 10, name = "Installed App").copy(
            validity = Validity(isInstalled = true),
            app = App(title = "Installed App", previewUrl = "https://preview.url"),
            tracking = Tracking(clickUrl = "https://click.url")
        )
        
        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle() // flush init fetchData
        
        viewModel.onOfferClick(offer, index = 0)
        advanceUntilIdle()

        // Should use previewUrl
        assertEquals("https://preview.url", viewModel.openUrlEvent.value)
        // Should NOT activate offer if already installed
        coVerify(exactly = 0) { mockNetworkCommons.activateOffer(any()) }
    }

    @Test
    fun onOfferClick_whenNotInstalled_activatesOfferAndOpensClickUrl() = runTest {
        val offer = makeOffer(id = 20, name = "New App").copy(
            validity = Validity(isInstalled = false, isRetryDownload = false),
            app = App(title = "New App", previewUrl = "https://preview.url"),
            tracking = Tracking(clickUrl = "https://click.url")
        )
        
        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()
        
        viewModel.onOfferClick(offer, index = 1)
        advanceUntilIdle()

        // Should use clickUrl
        assertEquals("https://click.url", viewModel.openUrlEvent.value)
        // Should track normal activation
        verify { mockTyrads.track(TyradsActivity.CAMPAIGN_ACTIVATED) }
        // Should call activateOffer
        coVerify(exactly = 1) { mockNetworkCommons.activateOffer("20") }
    }

    @Test
    fun onOfferClick_whenRetryDownload_tracksRetryAndActivates() = runTest {
        val offer = makeOffer(id = 30, name = "Retry App").copy(
            validity = Validity(isInstalled = false, isRetryDownload = true),
            app = App(title = "Retry App", previewUrl = "https://preview.url"),
            tracking = Tracking(clickUrl = "https://click.url")
        )
        
        val viewModel = TopOffersViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()
        
        viewModel.onOfferClick(offer, index = 2)
        advanceUntilIdle()

        // Should use clickUrl
        assertEquals("https://click.url", viewModel.openUrlEvent.value)
        // Should track retry activation
        verify { mockTyrads.track(TyradsActivity.CAMPAIGN_ACTIVATED_RETRY) }
        coVerify(exactly = 1) { mockNetworkCommons.activateOffer("30") }
    }
}
