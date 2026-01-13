package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tyrads.sdk.R

// Color definitions
private val TealTop = Color(0xFF02B5BE)

@Composable
fun CommonBonusCard(
    title: String,
    description: @Composable () -> Unit,
    onDismiss: () -> Unit,
    coinIconRes: Int,
    bottomLimitLabel: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopStart
    ) {
        // Main Card with solid gradient background (white to teal)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 35.dp)
                .wrapContentHeight()
                .zIndex(1f)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color.White,
                                Color(0xFFE0F7F7),
                                Color(0xFFB3EBEB),
                                Color(0xFF66D9D9),
                                Color(0xFF33CACA),
                                TealTop
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    // Header Section with space for coin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 8.dp)
                    ) {
                        // Title - with padding to avoid coin overlap, supports multi-line
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W900,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(start = 85.dp, end = 55.dp) // Left padding to avoid coin, right for close button
                                .offset(y = 15.dp)
                        )

                        // Close button on the right - positioned higher
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = (-2).dp)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.circle_x),
                                contentDescription = "Close",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.8.dp,
                        color = Color(0xFFE0E0E0)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        description()
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        content()
                    }

                    // Bottom limit label (if provided)
                    if (bottomLimitLabel != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 20.dp, bottom = 16.dp, top = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            bottomLimitLabel()
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Floating coin icon - positioned at specific angle on left side
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 15.dp)
                .zIndex(2f)
        ) {
            FloatingCoinIcon(coinIconRes = coinIconRes)
        }
    }
}

@Composable
fun FloatingCoinIcon(coinIconRes: Int) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .rotate(30f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(coinIconRes),
            contentDescription = "Bonus Coin",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}