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

class TopOffersViewModel(
    private val networkCommons: NetworkCommons = NetworkCommons()
) : ViewModel() {

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