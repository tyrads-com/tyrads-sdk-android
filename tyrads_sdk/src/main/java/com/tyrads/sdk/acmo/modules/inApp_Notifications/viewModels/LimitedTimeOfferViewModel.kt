package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.InAppNotificationManager
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.Campaign
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.GroupData
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.LimitedTimeEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LimitedTimeOfferViewModel : ViewModel() {

    private val networkCommons = NetworkCommons()

    private val _uiState = MutableStateFlow(LimitedOfferUiState())
    val uiState: StateFlow<LimitedOfferUiState> = _uiState.asStateFlow()

    init {
        fetchLimitedEvents()
    }

    private fun fetchLimitedEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val langCode = Tyrads.getInstance().currentLanguageCode.value
                val groupDataList = networkCommons.fetchLimitedEvents(langCode)
                val filteredCampaigns = filterLimitedOffers(groupDataList ?: emptyList())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    campaigns = filteredCampaigns
                )

                // Pass both visibility flag and whether there are actual events
                val hasEvents = filteredCampaigns.isNotEmpty()
                Log.d("filteredCampaigns", filteredCampaigns.toString())
                InAppNotificationManager.setLimitedTimeVisible(
                    visible = true, // Always try to show if fetched successfully
                    hasEvents = hasEvents // But only if there are events
                )
            } catch (e: Exception) {
                Tyrads.getInstance().log(
                    "Error fetching limited events: ${e.message}",
                    android.util.Log.ERROR
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    campaigns = emptyList()
                )

                // If there's an error, mark as shown (empty) so it doesn't keep trying
                InAppNotificationManager.setLimitedTimeVisible(
                    visible = true,
                    hasEvents = false
                )
            }
        }
    }

    private fun filterLimitedOffers(activeCampaigns: List<GroupData>): List<Campaign> {
        val filteredCampaigns = activeCampaigns
            .filter { campaign -> campaign.groupName?.lowercase() == "hotdeals" }
            .flatMap { it.campaigns ?: emptyList() }
            .filter {
                !it.limitedTimeEvents.isNullOrEmpty() &&
                        it.status?.lowercase() != "suspended" &&
                        it.isInstalled ?: false &&
                        it.limitedTimeEvents.any { e -> e.conversionStatus?.lowercase() != "approved" }
            }
        return filteredCampaigns
    }

    fun showCountDown(event: LimitedTimeEvent): Boolean {
        val allowDuplicateEvents = event.allowDuplicateEvents ?: false
        val conversionStatus = event.conversionStatus?.lowercase()
        val dailyCount = event.dailyCount
        val dailyLimit = event.dailyLimit
        val isDailyLimitIncomplete = dailyCount == null || dailyLimit == null || dailyCount < dailyLimit

        if (!allowDuplicateEvents) {
            return conversionStatus == null
        }
        if (isDailyLimitIncomplete) {
            return true
        }
        return false
    }

    fun getStatusString(event: LimitedTimeEvent): String {
        val allowDuplicateEvents = event.allowDuplicateEvents ?: false
        val conversionStatus = event.conversionStatus?.lowercase()
        val dailyCount = event.dailyCount
        val dailyLimit = event.dailyLimit
        if (!allowDuplicateEvents) {
            if (conversionStatus == "approved") {
                return "Completed"
            }
            if (conversionStatus == "rejected") {
                return "Rejected"
            }
        }
        val isDailyLimitComplete = dailyCount != null && dailyLimit != null && dailyCount >= dailyLimit

        if (allowDuplicateEvents && isDailyLimitComplete) {
            return "Completed"
        }
        return ""
    }

    fun onPageChanged(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }
}

data class LimitedOfferUiState(
    val isLoading: Boolean = false,
    val campaigns: List<Campaign> = emptyList(),
    val currentPage: Int = 0,
    val error: String? = null
)