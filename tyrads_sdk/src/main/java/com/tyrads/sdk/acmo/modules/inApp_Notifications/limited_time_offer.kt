package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tyrads.sdk.R
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.Campaign
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.LimitedTimeEvent
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.LimitedTimeOfferViewModel
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets.CommonBonusCard
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets.CountDownTimer
import com.tyrads.sdk.acmo.modules.premium_widgets.components.AcmoCarouselSlider

private val CyanButton = Color(0xFF00BCD4)
private val RedExpiry = Color(0xFFFF554A)
private val CreamBackground = Color(0xFFFFF9ED)

@Composable
fun LimitedTimeOfferScreen(
    viewModel: LimitedTimeOfferViewModel = viewModel(),
    onDismiss: () -> Unit = {},
    onPlayNow: (String) -> Unit = {},
    coinIconRes: Int = R.drawable.limited_offer_coin
) {
    val uiState by viewModel.uiState.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            CommonBonusCard(
                title = "Limited Time Offer",
                description = {
                    Text(
                        text = "Limited time offer unlocked! Play now and claim extra rewards!",
                        fontSize = 13.sp,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.W500,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                onDismiss = onDismiss,
                coinIconRes = coinIconRes,
                bottomLimitLabel = null
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CyanButton)
                    }
                } else if (uiState.campaigns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No limited time offers available",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                        ) {
                            AcmoCarouselSlider(
                                itemCount = uiState.campaigns.size,
                                itemBuilder = { index ->
                                    LimitedOfferCard(
                                        campaign = uiState.campaigns[index],
                                        onPlayNow = { onPlayNow(uiState.campaigns[index].app?.previewUrl ?: "") }
                                    )
                                },
                                autoPlayIntervalMs = 3000L,
                                showIndicator = false,
                                infiniteScroll = false,
                                viewportFraction = if(uiState.campaigns.size == 1) 0.95f else 0.86f,
                                scaleFactor = 1f,
                                onPageChanged = { page ->
                                    viewModel.onPageChanged(page)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            repeat(uiState.campaigns.size) { index ->
                                val isActive = uiState.currentPage % uiState.campaigns.size == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (isActive) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) Color(0xFFFFFFFF) else Color(0x80FFFFFF)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LimitedOfferCard(
    campaign: Campaign,
    onPlayNow: () -> Unit
) {
    val limitedEvents = campaign.limitedTimeEvents?.filter { it.isLimitedTimeEvent == true } ?: emptyList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.alarm_clock),
                contentDescription = null,
                modifier = Modifier
                    .size(185.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 15.dp, y = 15.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!campaign.app?.thumbnail.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.LightGray)
                        ) {
                            coil.compose.AsyncImage(
                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                    .data(campaign.app?.thumbnail)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Game Icon",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_coin2),
                                error = painterResource(R.drawable.ic_coin2)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF9800),
                                            Color(0xFFFF6F00)
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = campaign.app?.title ?: campaign.campaignName ?: "Unknown Game",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    limitedEvents.forEachIndexed { index, event ->
                        LimitedOfferTaskRow(
                            event = event,
                            adUnitCurrencyIcon = campaign.currency?.adUnitCurrencyIcon
                        )
                        if (index < limitedEvents.size - 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onPlayNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanButton
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Text(
                        text = "Play Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LimitedOfferTaskRow(event: LimitedTimeEvent, adUnitCurrencyIcon: String?, viewModel: LimitedTimeOfferViewModel = viewModel()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = event.eventName ?: "",
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                color = Color(0xFF4A4A4A),
                maxLines = 2,
                lineHeight = 14.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(y = (-1).dp)
            ) {
                if (!adUnitCurrencyIcon.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = adUnitCurrencyIcon,
                        contentDescription = "Coin",
                        modifier = Modifier.size(14.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(R.drawable.ic_coin2),
                        error = painterResource(R.drawable.ic_coin2)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_coin2),
                        contentDescription = "Coin",
                        modifier = Modifier.size(14.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = (event.payoutAmountConverted ?: 0).toDouble().numeral(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanButton
                )
            }
        }

        if (viewModel.showCountDown(event)){
            Box(
                modifier = Modifier
                    .background(
                        color = RedExpiry,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 0.dp)
            ) {
                CountDownTimer(
                    seconds = event.limitedTimeEventRemainingSeconds ?: 0,
                    fontSize = 11.sp,
                )
            }
        } else{
            Text(
                text = viewModel.getStatusString(event),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (event.conversionStatus?.lowercase() == "approved") Color(0xFFA3A9B6) else Color(0xFFFF554A)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LimitedTimeOfferScreenPreview() {
    MaterialTheme {
        LimitedTimeOfferScreen(
            onDismiss = {},
            onPlayNow = {}
        )
    }
}