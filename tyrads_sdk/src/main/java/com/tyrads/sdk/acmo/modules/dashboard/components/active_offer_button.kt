package com.tyrads.sdk.acmo.modules.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.R

@Composable
fun ActiveOfferButton(
    modifier: Modifier = Modifier,
    activatedCount: Int,
    onTap: () -> Unit = {
        // Default implementation - navigate to activated campaigns
        // In actual implementation, this would call:
        // Tyrads.instance.showOffers(
        //     context,
        //     route = TyradsDeepRoutes.CAMPAIGNS_ACTIVATED,
        //     launchMode = Tyrads.instance.launchMode,
        // )
    }
) {
    // Get premium color from theme - equivalent to Tyrads.instance.colorPremium ?? Theme.of(context).colorScheme.secondary
    val premiumColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    // InkWell equivalent with padding (Flutter's InkWell + Padding)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(16.dp)
    ) {
        // Expanded equivalent - takes full available width
        Box(
            modifier = Modifier
                .weight(1f) // Equivalent to Expanded
                .height(42.dp)
                .border(
                    width = 2.dp,
                    color = premiumColor,
                    shape = RoundedCornerShape(42.dp) // Pill shape - BorderRadius.circular(42)
                )
                .clip(RoundedCornerShape(42.dp))
                .background(onPrimaryColor),
            contentAlignment = Alignment.Center // Center equivalent
        ) {
            // Row with MainAxisSize.min equivalent
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main text
                Text(
                    text = stringResource(id = R.string.offers_active_offers_cta), // t.offers.activeOffersCta equivalent
                    color = premiumColor,
                    fontWeight = FontWeight.SemiBold, // FontWeight.w600 equivalent
                    fontSize = 14.sp
                )

                // Count badge with margin
                CountBadge(
                    activatedCount = activatedCount,
                    modifier = Modifier
                        .padding(start = 8.dp) // margin: EdgeInsets.only(left: 8) equivalent
                        .size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CountBadge(
    activatedCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFFF554A), // Same color as Flutter version
                shape = CircleShape // BoxShape.circle equivalent
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$activatedCount+",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold, // FontWeight.w700 equivalent
            maxLines = 1,
            softWrap = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ActiveOfferButtonPreview() {
    MaterialTheme {
        ActiveOfferButton(
            activatedCount = 5,
            onTap = {
                println("Active Offer Button Clicked!")
            }
        )
    }
}