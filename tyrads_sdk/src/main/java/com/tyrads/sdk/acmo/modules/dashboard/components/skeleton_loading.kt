package com.tyrads.sdk.acmo.modules.dashboard.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AcmoCustomSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
    width: Dp? = null, // null means fill available width (equivalent to double.maxFinite)
    borderRadius: Dp? = null
) {
    // Animation controller equivalent to Flutter's AnimationController
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_animation")

    // Color animation equivalent to Flutter's ColorTween
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFE0E2E7), // begin color
        targetValue = Color(0xFFC2C6CE),  // end color
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000, // Duration(milliseconds: 1000)
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse // repeat(reverse: true)
        ),
        label = "skeleton_color_animation"
    )

    // Container equivalent with animated background
    Box(
        modifier = modifier
            .let { mod ->
                if (width != null) {
                    mod.size(width = width, height = height)
                } else {
                    mod
                        .fillMaxWidth() // double.maxFinite equivalent
                        .height(height)
                }
            }
            .clip(
                RoundedCornerShape(borderRadius ?: 0.dp) // BorderRadius.circular(borderRadius ?? 0)
            )
            .background(animatedColor)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcmoSkeletonLoading() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Scaffold(
        bottomBar = {
            // BottomAppBar equivalent
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF0F1F3) // Same color as Flutter version
            ) {
                AcmoCustomSkeleton(
                    modifier = Modifier.padding(16.dp),
                    width = null, // double.maxFinite equivalent
                    height = 42.dp,
                    borderRadius = 42.dp
                )
            }
        }
    ) { paddingValues ->
        // SingleChildScrollView equivalent
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
            horizontalAlignment = Alignment.Start // crossAxisAlignment: CrossAxisAlignment.start
        ) {
            // First skeleton with padding
            AcmoCustomSkeleton(
                modifier = Modifier.padding(16.dp),
                height = 18.dp,
                width = 120.dp
            )

            // Row with spacing equivalent to Row(spacing: 16)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AcmoCustomSkeleton(
                    height = 225.dp,
                    width = 16.dp
                )
                AcmoCustomSkeleton(
                    height = 247.dp,
                    width = screenWidth - 64.dp // MediaQuery.of(context).size.width - 64
                )
                AcmoCustomSkeleton(
                    height = 225.dp,
                    width = 16.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // SizedBox(height: 16)

            // Centered skeleton - Align(alignment: Alignment.center)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AcmoCustomSkeleton(
                    width = 48.dp,
                    height = 8.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // SizedBox(height: 16)

            // Padded column with spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start, // crossAxisAlignment: CrossAxisAlignment.start
                verticalArrangement = Arrangement.spacedBy(16.dp) // spacing: 16
            ) {
                AcmoCustomSkeleton(
                    height = 18.dp,
                    width = 120.dp
                )

                // List.generate(4, ...) equivalent
                repeat(4) { index ->
                    AcmoCustomSkeleton(
                        height = 270.dp,
                        width = null // double.maxFinite equivalent
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AcmoCustomSkeletonPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Different skeleton variations
        AcmoCustomSkeleton(
            height = 50.dp,
            width = 200.dp
        )

        AcmoCustomSkeleton(
            height = 30.dp,
            width = 150.dp,
            borderRadius = 15.dp
        )

        AcmoCustomSkeleton(
            height = 60.dp,
            width = null, // Full width
            borderRadius = 8.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AcmoSkeletonLoadingPreview() {
    MaterialTheme {
        AcmoSkeletonLoading()
    }
}