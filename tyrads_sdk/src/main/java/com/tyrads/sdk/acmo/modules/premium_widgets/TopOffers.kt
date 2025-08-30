package com.tyrads.sdk.acmo.modules.premium_widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.Tyrads.PremiumWidgetStyles
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.core.services.LocalizationService
import com.tyrads.sdk.acmo.helpers.launchUrlForce
import com.tyrads.sdk.acmo.modules.input_models.cardElevation
import com.tyrads.sdk.acmo.modules.input_models.errorPadding
import com.tyrads.sdk.acmo.modules.premium_widgets.components.*
import com.tyrads.sdk.acmo.modules.premium_widgets.view_model.TopOffersViewModel
import com.tyrads.sdk.ui.theme.RedColor
import com.tyrads.sdk.ui.theme.WhiteColor
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TopOffers(
    widgetStyle: PremiumWidgetStyles = PremiumWidgetStyles.LIST,
    viewModel: TopOffersViewModel = viewModel()
) {
    val context: Context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val openUrlEvent by viewModel.openUrlEvent.collectAsState()
    val privacyAccepted = Tyrads.getInstance().privacyAccepted

    LaunchedEffect(openUrlEvent) {
        Tyrads.getInstance().initializePrivacyStatus()
        openUrlEvent?.let { url ->
            if (url.isNotBlank()) {
                launchUrlForce(context, url)
                viewModel.onUrlEventHandled()
            }
        }
    }

    val localizationService = remember { LocalizationService.getInstance() }

    if (uiState.isLoading && uiState.cachedHotOffers.isEmpty()) {
        PremiumWidgetLoading(widgetStyle = widgetStyle)
        return
    }

    if (uiState.error != null && uiState.cachedHotOffers.isEmpty()) {
        Text(
            text = "Error: ${uiState.error}",
            color = RedColor,
            modifier = Modifier.padding(errorPadding)
        )
        return
    }

    if (uiState.cachedHotOffers.isEmpty()) {
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
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                localizationService = localizationService
            )
            when (widgetStyle) {
                PremiumWidgetStyles.LIST -> {
                    uiState.cachedHotOffers.forEachIndexed { index, offer ->
                        AcmoOfferListItem(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = if (index != uiState.cachedHotOffers.size - 1) 16.dp else 0.dp
                            ),
                            offer = offer,
                            currencySales = uiState.currencySales,
                            onItemTap = {
                                coroutineScope.launch {
                                    Tyrads.getInstance()
                                        .showOffers(route = "offers/${offer.campaignId}")
                                }
                            },
                            onButtonTap = {
                                if (privacyAccepted.value) {
                                    viewModel.onOfferClick(offer, index)
                                } else {
                                    coroutineScope.launch {
                                        Tyrads.getInstance()
                                            .showOffers(route = "offers/${offer.campaignId}")
                                    }
                                }
                            },
                            index = index,
                            loadingIndex = uiState.loadingIndex,
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
                        itemCount = uiState.cachedHotOffers.size,
                        itemBuilder = { index ->
                            val offer = uiState.cachedHotOffers[index]
                            AcmoOfferCard(
                                item = offer,
                                currencySales = uiState.currencySales,
                                margin = PaddingValues(horizontal = 16.dp),
                                onButtonClick = {
                                    if (privacyAccepted.value) {
                                        viewModel.onOfferClick(offer, index)
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
                        viewportFraction = 1f,
                        indicatorActiveColor = Tyrads.getInstance().premiumColor.toColor(),
                        indicatorInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }

            ActiveOfferButton(
                activatedCount = uiState.activeOffersCount,
                onTap = {
                    coroutineScope.launch {
                        Tyrads.getInstance().showOffers(route = "active-offers")
                    }
                }
            )
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
                // Debug and fix the localization
                val translatedText = localizationService.translate("data.widget.button.continuePlaying")
                val buttonText = if (translatedText == "data.widget.button.continuePlaying") {
                    // Fallback if translation key doesn't exist
                    Log.w("TopOffers", "Translation key 'data.widget.button.continuePlaying' not found, using fallback")
                    localizationService.translate("data.widget.button.play") // Try alternative key
                        .takeIf { it != "data.widget.button.play" }
                        ?: "Continue Playing" // Final fallback
                } else {
                    translatedText
                }

                Log.d("TopOffers", "Original translation: '$translatedText', Final button text: '$buttonText'")

                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
