package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.CurrencySales
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.InAppNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BonusRewardsViewModel : ViewModel() {

    private val networkCommons = NetworkCommons()

    private val _uiState = MutableStateFlow(BonusRewardsUiState())
    val uiState: StateFlow<BonusRewardsUiState> = _uiState.asStateFlow()

    init {
        fetchCurrencySale()
    }

    private fun fetchCurrencySale() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val langCode = Tyrads.getInstance().currentLanguageCode.value
                val currencySale = networkCommons.fetchCurrencySale(langCode)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currencySale = currencySale
                )
                InAppNotificationManager.setCurrencySaleVisible(
                    currencySale != null
                )
            } catch (e: Exception) {
                Tyrads.getInstance().log(
                    "Error fetching currency sale: ${e.message}",
                    android.util.Log.ERROR
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

data class BonusRewardsUiState(
    val isLoading: Boolean = false,
    val currencySale: CurrencySales? = null,
    val error: String? = null
)