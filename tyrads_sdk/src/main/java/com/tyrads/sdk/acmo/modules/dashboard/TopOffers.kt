package com.tyrads.sdk.acmo.modules.dashboard

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.errorPadding
import com.tyrads.sdk.acmo.modules.input_models.loaderSize
import com.tyrads.sdk.acmo.modules.input_models.noCampaignPadding
import com.tyrads.sdk.ui.theme.RedColor

@Composable
fun TopOffers(
    showMore: Boolean,
    showMyOffers: Boolean,
    showMyOffersEmptyView: Boolean,
    style: Int = 2,
){
    val context: Context = LocalContext.current
    var banners by remember { mutableStateOf<List<BannerData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val networkCommons =
        remember { NetworkCommons() }

    LaunchedEffect(Unit) {
        val currentLanguage = LocalizationHelper.getLanguageCode(context)
        LocalizationHelper.applySavedLanguage(context = context)
        networkCommons.fetchCampaigns(
            onSuccess = {
                banners = it
                isLoading = false
            },
            onError = {
                error = it.message
                isLoading = false
            },
            langCode = currentLanguage
        )
    }

    when {
        isLoading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(loaderSize),
                color = MaterialTheme.colorScheme.primary
            )
        }

        error != null -> {
            Text(
                text = "Error: $error",
                color = RedColor,
                modifier = Modifier.padding(errorPadding)
            )
        }

        banners.isEmpty() -> {
            Text(
                text = "No campaigns available",
                modifier = Modifier.padding(noCampaignPadding)
            )
        }

        else -> {
            when(style){
                1 -> GameOffersScreen(banners)
                2 -> OffersScreen(
                    banners
                )
                3 -> OffersScreen3(
                    banners
                )
                4 -> OffersScreen4(
                    banners
                )
                else -> {
                    Text("Please specify correct style")
                }
            }
        }
    }
}