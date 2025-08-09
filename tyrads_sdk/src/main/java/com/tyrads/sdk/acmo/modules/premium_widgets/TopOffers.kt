package com.tyrads.sdk.acmo.modules.premium_widgets

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.Tyrads.PremiumWidgetStyles
import com.tyrads.sdk.acmo.modules.premium_widgets.components.AcmoCarouselSlider
import com.tyrads.sdk.acmo.modules.premium_widgets.components.ActiveOfferButton
import com.tyrads.sdk.acmo.modules.premium_widgets.components.AcmoOfferListItem
import com.tyrads.sdk.acmo.modules.premium_widgets.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.premium_widgets.components.PremiumWidgetLoading
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersModel
import com.tyrads.sdk.acmo.modules.input_models.cardCornerBottomEnd
import com.tyrads.sdk.acmo.modules.input_models.cardCornerBottomStart
import com.tyrads.sdk.acmo.modules.input_models.cardCornerTopEnd
import com.tyrads.sdk.acmo.modules.input_models.cardCornerTopStart
import com.tyrads.sdk.acmo.modules.input_models.cardElevation
import com.tyrads.sdk.acmo.modules.input_models.errorPadding
import com.tyrads.sdk.acmo.modules.premium_widgets.components.AcmoOfferCard
import com.tyrads.sdk.ui.theme.RedColor
import com.tyrads.sdk.ui.theme.WhiteColor
import kotlinx.coroutines.launch

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
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeOffersCount by remember { mutableIntStateOf(1) }
    var privacyAccepted by remember { mutableStateOf(true) }

    val networkCommons = remember { NetworkCommons() }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                cachedHotOffers = networkCommons.fetchCampaigns("en")
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
        EmptyOffersView()
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
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
            PremiumHeaderSection(
                showMore
            )
            // Header section
//            Spacer(modifier = Modifier.height(16.dp))

            // Content based on widget style
            when (widgetStyle) {
                PremiumWidgetStyles.LIST -> {
                    cachedHotOffers.forEachIndexed { index, offer ->
                        AcmoOfferListItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            offer = offer,
                            currencySales = null,
                            onItemTap = {
                                coroutineScope.launch {
                                    Tyrads.getInstance()
                                        .showOffers(route = "offers/${offer.campaignId}")
                                }
                            },
                            onButtonTap = {

                            },
                            index = index
                        )
                    }
                }

                PremiumWidgetStyles.SLIDER_CARDS -> {
                    AcmoCarouselSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        itemCount = cachedHotOffers.size,
                        itemBuilder = { index ->
                            AcmoOfferCard(
                                item = cachedHotOffers[index],
                                onButtonClick = TODO(),
                                currencySaleModel = TODO(),
                                itemScaleFactor = TODO(),
                                margin = TODO(),
                                isPremiumWidget = TODO(),
                                onTap = TODO()
                            )
                        },
                        showIndicator = true,
                        infiniteScroll = true,
                        viewportFraction = 0.9f,
                        scaleFactor = 0.94f,
                        indicatorActiveColor = MaterialTheme.colorScheme.primary,
                        indicatorInactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        onPageChanged = { index ->
                        }
                    )
                }
            }

//            Spacer(modifier = Modifier.height(cardGameListSpacing))

            // Bottom actions
            when {
                activeOffersCount == 0 -> {
                    // Show "See Other Offers" button
                    Button(
                        onClick = {

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

                showMyOffers -> {
                    // Show active offers button
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
private fun EmptyOffersView() {
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
                text = "Keep Playing!\nExciting Rewards Await!",
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
