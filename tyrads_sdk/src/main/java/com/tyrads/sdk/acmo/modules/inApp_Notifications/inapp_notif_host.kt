package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.helpers.launchUrlForce
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.BonusRewardsViewModel
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.LimitedTimeOfferViewModel
import kotlinx.coroutines.launch

@Composable
fun InAppNotificationHost() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val limitedTimeVM: LimitedTimeOfferViewModel = viewModel()
    val currencySaleVM: BonusRewardsViewModel = viewModel()

    limitedTimeVM.uiState.collectAsState()
    currencySaleVM.uiState.collectAsState()

    val activeDialog by InAppNotificationManager
        .activeDialog
        .collectAsState()

    when (activeDialog) {
        InAppDialogType.LIMITED_TIME -> {
            LimitedTimeOfferScreen(
                onDismiss = {
                    InAppNotificationManager.dismiss(
                        InAppDialogType.LIMITED_TIME
                    )
                },
                onPlayNow = { launchUrlForce(context, it) }
            )
        }

        InAppDialogType.CURRENCY_SALE -> {
            BonusRewardsUnlockedScreen(
                onDismiss = {
                    InAppNotificationManager.dismiss(
                        InAppDialogType.CURRENCY_SALE
                    )
                },
                onGoToOfferwall = {
                    InAppNotificationManager.dismiss(
                        InAppDialogType.CURRENCY_SALE
                    )
                    coroutineScope.launch {
                        Tyrads.getInstance().showOffers()
                    }
                }
            )
        }

        null -> Unit
    }
}
