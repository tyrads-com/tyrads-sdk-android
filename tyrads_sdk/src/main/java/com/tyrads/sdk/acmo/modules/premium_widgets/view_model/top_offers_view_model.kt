package com.tyrads.sdk.acmo.modules.premium_widgets.view_model

// TopOffersViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.Fuel
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersModel
import com.tyrads.sdk.acmo.modules.input_models.CurrencySales
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopOffersViewModel : ViewModel() {

    private val networkCommons = NetworkCommons()

    private val _uiState = MutableStateFlow(TopOffersUiState())
    val uiState: StateFlow<TopOffersUiState> = _uiState.asStateFlow()

    private val _openUrlEvent = MutableStateFlow<String?>(null)
    val openUrlEvent: StateFlow<String?> = _openUrlEvent.asStateFlow()

    init {
        viewModelScope.launch {
            Tyrads.getInstance().currentLanguageCode.collect { lang ->
                fetchData(lang)
            }
        }

        // Re-fetch the active offers count whenever the Offerwall (AcmoApp) is closed.
        // This ensures the counter on the Premium Widget syncs after campaigns are
        // activated inside the Offerwall WebView.
        viewModelScope.launch {
            Tyrads.getInstance().offerwallClosedSignal.collect { signal ->
                if (signal > 0L) {
                    refreshActiveOffersCount()
                }
            }
        }
    }

    private fun fetchData(lang: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val campaigns = networkCommons.fetchCampaigns(lang)
                val currencySales = networkCommons.fetchCurrencySale(lang)
                val activeOffersCount = networkCommons.fetchActiveOffersSummary(lang)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cachedHotOffers = campaigns,
                    currencySales = currencySales,
                    activeOffersCount = activeOffersCount
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Lightweight refresh that only updates the active offers counter badge.
     * Does NOT show a loading state or reload offer cards — called silently
     * after the Offerwall closes so the counter syncs without disrupting the UI.
     */
    private fun refreshActiveOffersCount() {
        viewModelScope.launch {
            try {
                val count = networkCommons.fetchActiveOffersSummary(
                    Tyrads.getInstance().currentLanguageCode.value
                )
                _uiState.value = _uiState.value.copy(activeOffersCount = count)
            } catch (e: Exception) {
                // Silently ignore — counter will refresh on next full fetch
                Log.w("TopOffersViewModel", "Failed to refresh active offers count: ${e.message}")
            }
        }
    }

    fun onOfferClick(campaign: AcmoOffersModel, index: Int) {
        viewModelScope.launch {
            setLoadingIndex(index)
            var url: String = campaign.tracking.clickUrl ?: ""
            try {
                if (campaign.isInstalled) {
                    url = campaign.app.previewUrl
                } else {
                    if (campaign.isRetryDownload) {
                        Tyrads.getInstance().track(TyradsActivity.CAMPAIGN_ACTIVATED_RETRY)
                    } else {
                        Tyrads.getInstance().track(TyradsActivity.CAMPAIGN_ACTIVATED)
                    }
                    networkCommons.activateOffer(id = campaign.campaignId.toString())
                }

                if (!campaign.tracking.s2sClickUrl.isNullOrEmpty()) {
                    try {
                        Fuel.get(campaign.tracking.s2sClickUrl)
                    } catch (e: Exception) {
                        Log.e(
                            "S2S_CLICK_ERROR",
                            "Failed to send S2S click for campaign ${campaign.campaignId}",
                            e
                        )
                    }
                }

                _openUrlEvent.value = url.takeIf { it.isNotBlank() }
                fetchData(Tyrads.getInstance().currentLanguageCode.value)
            } catch (e: Exception) {
                Log.e("OPEN_OFFER_ERROR", "Error opening offer: ${e.message}", e)
            } finally {
                setLoadingIndex(null)
            }
        }
    }

    fun setLoadingIndex(index: Int?) {
        _uiState.value = _uiState.value.copy(loadingIndex = index)
    }

    fun onUrlEventHandled() {
        _openUrlEvent.value = null
    }
}

data class TopOffersUiState(
    val isLoading: Boolean = false,
    val cachedHotOffers: List<AcmoOffersModel> = emptyList(),
    val currencySales: CurrencySales? = null,
    val activeOffersCount: Int = 0,
    val error: String? = null,
    val loadingIndex: Int? = null
)