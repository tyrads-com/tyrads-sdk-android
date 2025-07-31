package com.tyrads.sdk.acmo.modules.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PremiumWidgetLoading(
    modifier: Modifier = Modifier,
    widgetStyle: PremiumWidgetStyles,
    itemHeight: Float = 200f
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // BorderRadius.circular(16)
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header row with two skeleton placeholders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AcmoCustomSkeleton(
                    width = 120.dp,
                    height = 18.dp
                )
                AcmoCustomSkeleton(
                    width = 120.dp,
                    height = 18.dp
                )
            }

            // Content based on widget style
            when (widgetStyle) {
                PremiumWidgetStyles.LIST -> {
                    // Generate 4 list items - equivalent to List.generate(4, ...)
                    repeat(4) { index ->
                        ListItemSkeleton()
                    }
                }

                else -> {
                    // For slider cards and other styles
                    AcmoCustomSkeleton(
                        width = null, // double.maxFinite equivalent (fills available width)
                        height = itemHeight.dp
                    )
                }
            }

            // Slider indicator (only for slider cards)
            if (widgetStyle == PremiumWidgetStyles.SLIDER_CARDS) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AcmoCustomSkeleton(
                        width = 50.dp,
                        height = 8.dp
                    )
                }
            }

            // Bottom button skeleton
            AcmoCustomSkeleton(
                width = null, // double.maxFinite equivalent
                height = 42.dp,
                borderRadius = 21.dp // borderRadius: 21
            )
        }
    }
}

@Composable
private fun ListItemSkeleton() {
    // Equivalent to Flutter's ListTile with custom content
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Standard ListTile spacing
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading - equivalent to ListTile.leading
        AcmoCustomSkeleton(
            width = 54.dp,
            height = 54.dp,
            borderRadius = 4.dp
        )

        Spacer(modifier = Modifier.width(16.dp)) // Standard ListTile spacing

        // Title - equivalent to ListTile.title
        Column(
            modifier = Modifier.weight(1f), // Takes remaining space
            verticalArrangement = Arrangement.spacedBy(2.dp) // spacing: 2
        ) {
            AcmoCustomSkeleton(
                width = 130.dp,
                height = 16.dp
            )
            AcmoCustomSkeleton(
                width = 65.dp,
                height = 16.dp
            )
        }

        Spacer(modifier = Modifier.width(16.dp)) // Standard ListTile spacing

        // Trailing - equivalent to ListTile.trailing
        AcmoCustomSkeleton(
            width = 80.dp,
            height = 42.dp,
            borderRadius = 8.dp
        )
    }
}

// Enum for widget styles (if not already defined)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PremiumWidgetLoadingPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PremiumWidgetLoading(
            widgetStyle = PremiumWidgetStyles.LIST,
            itemHeight = 200f
        )

        PremiumWidgetLoading(
            widgetStyle = PremiumWidgetStyles.SLIDER_CARDS,
            itemHeight = 300f
        )
    }
}