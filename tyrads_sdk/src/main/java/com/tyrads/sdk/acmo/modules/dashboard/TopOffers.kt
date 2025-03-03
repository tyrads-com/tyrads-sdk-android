package com.tyrads.sdk.acmo.modules.dashboard

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.tyrads.sdk.acmo.modules.dashboard.components.MyGamesButton
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.cardCornerBottomEnd
import com.tyrads.sdk.acmo.modules.input_models.cardCornerBottomStart
import com.tyrads.sdk.acmo.modules.input_models.cardCornerTopEnd
import com.tyrads.sdk.acmo.modules.input_models.cardCornerTopStart
import com.tyrads.sdk.acmo.modules.input_models.cardElevation
import com.tyrads.sdk.acmo.modules.input_models.cardGameListSpacing
import com.tyrads.sdk.acmo.modules.input_models.cardPaddingHorizontal
import com.tyrads.sdk.acmo.modules.input_models.cardPaddingVertical
import com.tyrads.sdk.acmo.modules.input_models.errorPadding
import com.tyrads.sdk.acmo.modules.input_models.headerTextSpacing
import com.tyrads.sdk.acmo.modules.input_models.loaderSize
import com.tyrads.sdk.acmo.modules.input_models.noCampaignPadding
import com.tyrads.sdk.ui.theme.RedColor
import com.tyrads.sdk.ui.theme.WhiteColor

@Composable
fun TopOffers(
    showMore: Boolean,
    showMyOffers: Boolean,
    showMyOffersEmptyView: Boolean,
    style: Int = 2,
){
    val context: Context = LocalContext.current
    var campaigns by remember { mutableStateOf<List<BannerData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val networkCommons =
        remember { NetworkCommons() }

    LaunchedEffect(Unit) {
        val currentLanguage = LocalizationHelper.getLanguageCode(context)
        LocalizationHelper.applySavedLanguage(context = context)
        networkCommons.fetchCampaigns(
            onSuccess = {
                campaigns = it
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

        campaigns.isEmpty() -> {
           if(showMyOffersEmptyView) {Text(
                text = "No campaigns available",
                modifier = Modifier.padding(noCampaignPadding)
            )}else{
                Box{}
           }
        }

        else -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        horizontal = cardPaddingHorizontal,
                        vertical = cardPaddingVertical
                    ),
                shape = RoundedCornerShape(
                    topStart = cardCornerTopStart,
                    topEnd = cardCornerTopEnd,
                    bottomEnd = cardCornerBottomEnd,
                    bottomStart = cardCornerBottomStart
                ),
                colors = CardDefaults.cardColors(containerColor = WhiteColor),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    PremiumHeaderSection(
                        showMore
                    )
                    Spacer(modifier = Modifier.height(headerTextSpacing))
                    when(style){
                        1 -> GameOffersScreen(campaigns)
                        2 -> OffersScreen(
                            campaigns
                        )
                        3 -> OffersScreen3(
                            campaigns
                        )
                        4 -> OffersScreen4(
                            campaigns
                        )
                        else -> {
                            Text("Please specify correct style")
                        }
                    }
                    Spacer(modifier = Modifier.height(cardGameListSpacing))
                    if(showMyOffers){
                        MyGamesButton()
                    }
                }
            }

        }
    }
}