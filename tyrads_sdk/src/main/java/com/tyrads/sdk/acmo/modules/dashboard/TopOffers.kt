package com.tyrads.sdk.acmo.modules.dashboard

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.R
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper
import com.tyrads.sdk.acmo.modules.dashboard.components.AcmoCarouselSlider
import com.tyrads.sdk.acmo.modules.dashboard.components.ActiveOfferButton
import com.tyrads.sdk.acmo.modules.dashboard.components.AcmoOfferListItem
import com.tyrads.sdk.acmo.modules.dashboard.components.AcmoOffersModel
import com.tyrads.sdk.acmo.modules.dashboard.components.AppDetails
import com.tyrads.sdk.acmo.modules.dashboard.components.CampaignPayout
import com.tyrads.sdk.acmo.modules.dashboard.components.CurrencyInfo
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumWidgetLoading
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumWidgetStyles
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
    showMore: Boolean = true,
    showMyOffers: Boolean = true,
    showMyOffersEmptyView: Boolean = false,
    widgetStyle: PremiumWidgetStyles = PremiumWidgetStyles.LIST,
) {
    val context: Context = LocalContext.current
    var cachedHotOffers by remember { mutableStateOf<List<BannerData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeOffersCount by remember { mutableIntStateOf(1) }
    var privacyAccepted by remember { mutableStateOf(true) } // This should come from actual privacy settings

    val networkCommons = remember { NetworkCommons() }

    LaunchedEffect(Unit) {
        loadData(
            networkCommons = networkCommons,
            context = context,
            onSuccess = { campaigns ->
                cachedHotOffers = campaigns
                isLoading = false
                activeOffersCount = campaigns.size
            },
            onError = { errorMessage ->
                error = errorMessage
                isLoading = false
            }
        )
    }

    // Loading state
    if (isLoading && cachedHotOffers.isEmpty()) {
        PremiumWidgetLoading(
            widgetStyle = widgetStyle
        )
        return
    }

    // Error state
    if (error != null && cachedHotOffers.isEmpty()) {
        Text(
            text = "Error: $error",
            color = RedColor,
            modifier = Modifier.padding(errorPadding)
        )
        return
    }

    // Empty state or forced empty view
    if (cachedHotOffers.isEmpty() || showMyOffersEmptyView) {
        EmptyOffersView()
        return
    }

    // Main content
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
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header section
            Spacer(modifier = Modifier.height(headerTextSpacing))

            // Content based on widget style
            when (widgetStyle) {
                PremiumWidgetStyles.LIST -> {
                    // List style offers
                    cachedHotOffers.forEachIndexed { index, offer ->
                        AcmoOfferListItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            offer = convertBannerDataToAcmoOffersModel(offer),
                            currencySales = null,
                            onItemTap = {
                                handleOfferClick(offer, privacyAccepted)
                            },
                            onButtonTap = {
                                handleOfferClick(offer, privacyAccepted)
                            },
                            index = index
                        )
                    }
                }

                PremiumWidgetStyles.SLIDER_CARDS -> {
                    // Slider cards style
                    AcmoCarouselSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        itemCount = cachedHotOffers.size,
                        itemBuilder = { index ->
                            OfferCardItem(
                                offer = cachedHotOffers[index],
                                onOfferClick = {
                                    handleOfferClick(cachedHotOffers[index], privacyAccepted)
                                }
                            )
                        },
                        showIndicator = true,
                        infiniteScroll = true,
                        viewportFraction = 0.9f,
                        scaleFactor = 0.94f,
                        indicatorActiveColor = MaterialTheme.colorScheme.primary,
                        indicatorInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        onPageChanged = { index ->
                            // Handle page changes if needed
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(cardGameListSpacing))

            // Bottom actions
            when {
                activeOffersCount == 0 -> {
                    // Show "See Other Offers" button
                    Button(
                        onClick = {
                            // Navigate to offers screen
                            // Tyrads.instance.showOffers(context)
                        },
                        colors = ButtonDefaults.textButtonColors(),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "See Other Offers",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                showMyOffers && activeOffersCount > 0 -> {
                    // Show active offers button
                    ActiveOfferButton(
                        activatedCount = activeOffersCount,
                        onTap = {
                            // Navigate to active offers screen
                            // Tyrads.instance.showActiveOffers(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyOffersView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.diamond),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Keep Playing!\nExciting Rewards Await!",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WhiteColor
            )

            Button(
                onClick = {
                    // Navigate to offers screen
                    // Tyrads.instance.showOffers(context)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhiteColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Continue Playing",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun OfferCardItem(
    offer: BannerData,
    onOfferClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onOfferClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = offer.title ?: "Game Offer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap to Play",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private suspend fun loadData(
    networkCommons: NetworkCommons,
    context: Context,
    onSuccess: (List<BannerData>) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val currentLanguage = LocalizationHelper.getLanguageCode(context)
        LocalizationHelper.applySavedLanguage(context = context)

        networkCommons.fetchCampaigns(
            onSuccess = { campaigns ->
                onSuccess(campaigns)
            },
            onError = { exception ->
                onError(exception.message ?: "Unknown error occurred")
            },
            langCode = currentLanguage
        )
    } catch (e: Exception) {
        onError(e.message ?: "Failed to load data")
    }
}

private fun convertBannerDataToAcmoOffersModel(bannerData: BannerData): AcmoOffersModel {
    return AcmoOffersModel(
        campaignId = bannerData.campaignId.toString(),
        app = AppDetails(
            title = bannerData.title ?: "Unknown Game",
            thumbnail = bannerData.thumbnail
        ),
        campaignPayout = CampaignPayout(
            totalPlayablePayoutConverted = bannerData.points.toDouble()
        ),
        currency = CurrencyInfo(
            adUnitCurrencyIcon = bannerData.currency.toString()
        )
    )
}

private fun handleOfferClick(offer: BannerData, privacyAccepted: Boolean) {
    if (privacyAccepted) {
        // Handle direct offer opening with tracking
        // This should include the tracking logic similar to Flutter version
    } else {
        // Navigate to campaign details
        // Tyrads.instance.showOffers(context, campaignID, route)
    }
}