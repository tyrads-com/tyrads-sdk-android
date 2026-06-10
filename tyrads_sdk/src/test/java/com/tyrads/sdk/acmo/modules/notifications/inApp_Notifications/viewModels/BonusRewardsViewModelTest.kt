package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels

import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.CurrencySales
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.BonusRewardsViewModel
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.BonusRewardsUiState
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
 * Unit tests for [BonusRewardsViewModel] with mocked [NetworkCommons].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BonusRewardsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockNetworkCommons: NetworkCommons
    private lateinit var mockTyrads: Tyrads

    @Before
    fun setUp() {
        // Must be set BEFORE ViewModel creation
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
    fun bonusRewardsUiState_defaultValues_areCorrect() {
        val state = BonusRewardsUiState()
        assertFalse(state.isLoading)
        assertNull(state.currencySale)
        assertNull(state.error)
    }

    @Test
    fun initialState_beforeCoroutinesRun_hasDefaults() {
        val viewModel = BonusRewardsViewModel(networkCommons = mockNetworkCommons)
        // Before advancing, check state structure exists
        assertNotNull(viewModel.uiState.value)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun fetchCurrencySale_onSuccess_populatesCurrencySale() = runTest {
        val expectedSale = CurrencySales(
            name = "Gold Rush",
            multiplier = 2.0,
            remainingTimeSeconds = 3600
        )
        coEvery { mockNetworkCommons.fetchCurrencySale("en") } returns expectedSale

        val viewModel = BonusRewardsViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(expectedSale, state.currencySale)
        assertNull(state.error)
    }

    @Test
    fun fetchCurrencySale_onNullResponse_currencySaleIsNull() = runTest {
        coEvery { mockNetworkCommons.fetchCurrencySale("en") } returns null

        val viewModel = BonusRewardsViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.currencySale)
        assertNull(state.error)
    }

    @Test
    fun fetchCurrencySale_onException_populatesError() = runTest {
        coEvery { mockNetworkCommons.fetchCurrencySale("en") } throws RuntimeException("Server error")

        val viewModel = BonusRewardsViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.currencySale)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Server error"))
    }

    @Test
    fun fetchCurrencySale_afterSuccess_isLoadingIsFalse() = runTest {
        coEvery { mockNetworkCommons.fetchCurrencySale("en") } returns null

        val viewModel = BonusRewardsViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun fetchCurrencySale_callsNetworkOnce() = runTest {
        coEvery { mockNetworkCommons.fetchCurrencySale("en") } returns null

        val viewModel = BonusRewardsViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        coVerify(exactly = 1) { mockNetworkCommons.fetchCurrencySale("en") }
    }

    @Test
    fun fetchCurrencySale_withMultiplierOf2_returnsCorrectMultiplier() = runTest {
        val sale = CurrencySales(name = "2x Coins", multiplier = 2.0)
        coEvery { mockNetworkCommons.fetchCurrencySale("en") } returns sale

        val viewModel = BonusRewardsViewModel(networkCommons = mockNetworkCommons)
        advanceUntilIdle()

        assertEquals(2.0, viewModel.uiState.value.currencySale?.multiplier)
    }
}
