package com.tyrads.sdk.acmo.modules.premium_widgets.components

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
    }
) {
    val premiumColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .border(
                    width = 2.dp,
                    color = premiumColor,
                    shape = RoundedCornerShape(42.dp)
                )
                .clip(RoundedCornerShape(42.dp))
                .clickable(onClick = onTap)
                .background(onPrimaryColor),
            contentAlignment = Alignment.Center
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

                if (activatedCount > 0)
                    CountBadge(
                        activatedCount = activatedCount,
                        modifier = Modifier
                            .padding(start = 8.dp)
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
            text = if (activatedCount > 0 && activatedCount <= 99) "$activatedCount" else "99+",
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