package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.viewModels.BonusRewardsViewModel
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets.CommonBonusCard
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets.CountDownTimer
import kotlinx.coroutines.delay

private val RedExpiry = Color(0xFFFF554A)

@Composable
fun BonusRewardsUnlockedScreen(
    viewModel: BonusRewardsViewModel = viewModel(),
    onDismiss: () -> Unit = {},
    onGoToOfferwall: () -> Unit = {},
    coinIconRes: Int = R.drawable.bonus_rewards_coin
) {
    val uiState by viewModel.uiState.collectAsState()
    val mainColor = Tyrads.getInstance().mainColor?.let { Color(it.toColorInt()) } ?: Color(0xFF02B5BE)

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
                .background(Color.Black.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center
        ) {
            CommonBonusCard(
                title = "Bonus Rewards Unlocked!",
                description = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = mainColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            val multiplierText = uiState.currencySale?.multiplier

                            Text(
                                text = "You get ${multiplierText}X bonus rewards!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W900,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF1A1A1A),
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.3).sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Text(
                                text = "Go to offerwall and activate new offer!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W500,
                                color = Color(0xFF1A1A1A),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                },
                onDismiss = onDismiss,
                coinIconRes = coinIconRes,
                bottomLimitLabel = null
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = mainColor)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = RedExpiry,
                                    shape = RoundedCornerShape(25.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row {
                                Text(
                                    text = "Bonus Expires in ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                CountDownTimer(
                                    seconds = uiState.currencySale?.remainingTimeSeconds ?: 0,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = onGoToOfferwall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = mainColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = mainColor
                            ),
                            shape = RoundedCornerShape(25.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = "Go to Offerwall",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = mainColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BonusRewardsUnlockedScreenPreview() {
    MaterialTheme {
        BonusRewardsUnlockedScreen(
            onDismiss = {},
            onGoToOfferwall = {}
        )
    }
}