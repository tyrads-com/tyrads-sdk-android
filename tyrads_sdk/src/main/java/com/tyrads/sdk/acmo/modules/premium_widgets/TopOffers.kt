package com.tyrads.sdk.acmo.modules.premium_widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.kittinunf.fuel.Fuel
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.Tyrads.PremiumWidgetStyles
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.core.services.LocalizationService
import com.tyrads.sdk.acmo.helpers.launchUrlForce
import com.tyrads.sdk.acmo.modules.premium_widgets.components.AcmoCarouselSlider
import com.tyrads.sdk.acmo.modules.premium_widgets.components.ActiveOfferButton
import com.tyrads.sdk.acmo.modules.premium_widgets.components.AcmoOfferListItem
import com.tyrads.sdk.acmo.modules.premium_widgets.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.premium_widgets.components.PremiumWidgetLoading
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersModel
import com.tyrads.sdk.acmo.modules.input_models.CurrencySales
import com.tyrads.sdk.acmo.modules.input_models.cardElevation
import com.tyrads.sdk.acmo.modules.input_models.errorPadding
import com.tyrads.sdk.acmo.modules.premium_widgets.components.AcmoOfferCard
import com.tyrads.sdk.ui.theme.RedColor
import com.tyrads.sdk.ui.theme.WhiteColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TopOffers(
    showMore: Boolean = true,
    showMyOffers: Boolean = true,
    showMyOffersEmptyView: Boolean = false,
    widgetStyle: PremiumWidgetStyles = PremiumWidgetStyles.LIST,
) {
    val context: Context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var cachedHotOffers by remember { mutableStateOf<List<AcmoOffersModel>>(emptyList()) }
    var currencySales by remember { mutableStateOf<CurrencySales?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeOffersCount by remember { mutableIntStateOf(0) }

    // Loading state management
    var loadingIndex by remember { mutableStateOf<Int?>(null) }

    // LocalizationService instance
    val localizationService = remember { LocalizationService.getInstance() }

    val privacyAccepted = remember {
        Tyrads.getInstance().preferences.getBoolean(
            AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + Tyrads.getInstance().publisherUserID,
            false
        )
    }
    val networkCommons = remember { NetworkCommons() }

    // Initialize localization service when the composable is first created
    LaunchedEffect(Unit) {
        // Initialize localization service with current locale
        // You can get the current locale from system or user preferences
        val currentLocale = java.util.Locale.getDefault().language
        localizationService.init(currentLocale)
    }

    suspend fun openOffer(campaign: AcmoOffersModel) = withContext(Dispatchers.Default) {
        coroutineScope.launch {
            try {
                val url = if (campaign.isInstalled) {
                    campaign.app.previewUrl
                } else {
                    // Tyrads.getInstance().track(TyradsActivity.campaignActivated) // Assuming a tracking method
                    networkCommons.activateOffer(id = campaign.campaignId.toString())
                    campaign.tracking.clickUrl ?: ""
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

                // Launch the final URL in a browser or app that can handle it.
                if (url.isNotBlank()) {
                    launchUrlForce(context, url)
                } else {
                    Log.w(
                        "OpenOffer",
                        "URL is blank for campaign ${campaign.campaignId}, cannot launch."
                    )
                }

            } catch (e: Exception) {
                Log.e("OPEN_OFFER_ERROR", "Error opening offer: ${e.message}", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                cachedHotOffers = networkCommons.fetchCampaigns("en")
                currencySales = networkCommons.fetchCurrencySale("en")
                activeOffersCount = networkCommons.fetchActiveOffersSummary("en")
                isLoading = false
            } catch (e: Exception) {
                Log.e("FetchCampaigns", "Error: ${e.message}", e)
            }
        }
    }

    if (isLoading && cachedHotOffers.isEmpty()) {
        PremiumWidgetLoading(
            widgetStyle = widgetStyle
        )
        return
    }

    if (error != null && cachedHotOffers.isEmpty()) {
        Text(
            text = "Error: $error",
            color = RedColor,
            modifier = Modifier.padding(errorPadding)
        )
        return
    }

    if (cachedHotOffers.isEmpty() || showMyOffersEmptyView) {
        EmptyOffersView(localizationService = localizationService)
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            PremiumHeaderSection(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                showMore = showMore
            )
            when (widgetStyle) {
                PremiumWidgetStyles.LIST -> {
                    cachedHotOffers.forEachIndexed { index, offer ->
                        AcmoOfferListItem(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = if (index != cachedHotOffers.size - 1) 16.dp else 0.dp
                            ),
                            offer = offer,
                            currencySales = currencySales,
                            onItemTap = {
                                coroutineScope.launch {
                                    Tyrads.getInstance()
                                        .showOffers(route = "offers/${offer.campaignId}")
                                }
                            },
                            onButtonTap = {
                                if (privacyAccepted) {
                                    coroutineScope.launch {
                                        openOffer(offer)
                                    }
                                } else {
                                    coroutineScope.launch {
                                        Tyrads.getInstance()
                                            .showOffers(route = "offers/${offer.campaignId}")
                                    }
                                }
                            },
                            index = index,
                            loadingIndex = loadingIndex,
                            onLoadingIndexChange = { newLoadingIndex ->
                                loadingIndex = newLoadingIndex
                            }
                        )
                    }
                }

                PremiumWidgetStyles.SLIDER_CARDS -> {
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    val itemScaleFactor = 3.1
                    val itemHeight = screenWidth.value / itemScaleFactor
                    AcmoCarouselSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((itemHeight + 150).dp)
                            .padding(bottom = 8.dp),
                        itemCount = cachedHotOffers.size,
                        itemBuilder = { index ->
                            val offer = cachedHotOffers[index]
                            AcmoOfferCard(
                                item = offer,
                                currencySales = currencySales,
                                margin = PaddingValues(all = 16.dp),
                                onButtonClick = {
                                    if (privacyAccepted) {
                                        coroutineScope.launch {
                                            openOffer(offer)
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            Tyrads.getInstance()
                                                .showOffers(route = "offers/${offer.campaignId}")
                                        }
                                    }
                                },
                                onTap = {
                                    coroutineScope.launch {
                                        Tyrads.getInstance()
                                            .showOffers(route = "offers/${offer.campaignId}")
                                    }
                                },
                            )
                        },
                        showIndicator = true,
                        infiniteScroll = true,
                        viewportFraction = 1.0f,
                        indicatorActiveColor = Tyrads.getInstance().premiumColor.toColor(),
                        indicatorInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        onPageChanged = { index ->
                        }
                    )
                }
            }

            when {
                showMyOffers -> {
                    ActiveOfferButton(
                        activatedCount = activeOffersCount,
                        onTap = {
                            coroutineScope.launch {
                                Tyrads.getInstance().showOffers(route = "active-offers")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyOffersView(
    localizationService: LocalizationService
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.premium_empty_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Inside
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = localizationService.translate("data.widget.empty.noOffers"),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WhiteColor
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        Tyrads.getInstance().showOffers()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhiteColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                val translatedText = localizationService.translate("data.widget.button.continuePlaying")
                Text(
                    text = translatedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Log.d("TopOffers", "Localized continue playing text: $translatedText")
            }
        }
    }
}

@Composable
private fun OfferCardItem(
    offer: AcmoOffersModel,
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
                text = offer.app.title,
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