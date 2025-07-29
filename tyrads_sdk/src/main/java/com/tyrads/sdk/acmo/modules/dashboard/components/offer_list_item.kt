package com.tyrads.sdk.acmo.modules.dashboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.DecimalFormat
import com.tyrads.sdk.R

// Data Models (for context)
data class AcmoOffersModel(
    val campaignId: String, val app: AppDetails,
    val campaignPayout: CampaignPayout, val currency: CurrencyInfo
)
data class AppDetails(val title: String, val thumbnail: String)
data class CampaignPayout(val totalPlayablePayoutConverted: Double)
data class CurrencyInfo(val adUnitCurrencyIcon: String)
data class CurrencySales(val multiplier: Double)


@Composable
fun AcmoOfferListItem(
    modifier: Modifier = Modifier,
    offer: AcmoOffersModel,
    currencySales: CurrencySales?,
    onItemTap: () -> Unit,
    onButtonTap: () -> Unit,
    index: Int,
) {
    val premiumColor = MaterialTheme.colorScheme.primary
    val premiumFgColor = MaterialTheme.colorScheme.onPrimary

    Box(modifier = modifier.padding(start = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onItemTap)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OfferThumbnail(
                thumbnailUrl = offer.app.thumbnail,
                title = offer.app.title
            )

            Spacer(modifier = Modifier.width(12.dp))

            OfferDetails(
                modifier = Modifier.weight(1f),
                offer = offer,
                currencySales = currencySales,
                premiumColor = premiumColor
            )

            Spacer(modifier = Modifier.width(8.dp))

            PlayButton(
                onButtonTap = onButtonTap,
                premiumColor = premiumColor,
                premiumFgColor = premiumFgColor
            )
        }

        RankIcon(index = index)
    }
}
@Composable
private fun RankIcon(index: Int) {
    val rankAssets: List<Int> = listOf(
        R.drawable.rank_1,
        R.drawable.rank_2,
        R.drawable.rank_3,
        R.drawable.rank_4,
        R.drawable.rank_5
    )

    if (index in rankAssets.indices) {
        Image(
            painter = painterResource(id = rankAssets[index]),
            contentDescription = "Rank ${index + 1}",
            modifier = Modifier
                .offset(x = (-10).dp, y = 3.dp)
                .size(24.dp)
        )
    }
}

@Composable
private fun OfferThumbnail(thumbnailUrl: String, title: String) {
    AsyncImage(
        model = thumbnailUrl,
        contentDescription = "Offer thumbnail for $title",
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun OfferDetails(
    modifier: Modifier = Modifier,
    offer: AcmoOffersModel,
    currencySales: CurrencySales?,
    premiumColor: Color
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        currencySales?.let {
            BonusLabel(
                multiplier = it.multiplier,
                premiumColor = premiumColor
            )
        }

        Text(
            text = offer.app.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
        )

        OfferPayout(
            offer = offer,
            currencySales = currencySales
        )
    }
}

@Composable
private fun BonusLabel(multiplier: Double, premiumColor: Color) {
    Box(
        modifier = Modifier
            .background(
                color = premiumColor.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${multiplier}x BONUS",
            color = premiumColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun OfferPayout(offer: AcmoOffersModel, currencySales: CurrencySales?) {
    val numberFormat = DecimalFormat("#,##0.##")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        currencySales?.let {
            Text(
                text = numberFormat.format(offer.campaignPayout.totalPlayablePayoutConverted),
                color = Color(0xFF323434),
                textDecoration = TextDecoration.LineThrough,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }

        AsyncImage(
            model = offer.currency.adUnitCurrencyIcon,
            contentDescription = "Currency Icon",
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = numberFormat.format(
                offer.campaignPayout.totalPlayablePayoutConverted * (currencySales?.multiplier ?: 1.0)
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlayButton(onButtonTap: () -> Unit, premiumColor: Color, premiumFgColor: Color) {
    Button(
        onClick = onButtonTap,
        modifier = Modifier.sizeIn(minWidth = 75.dp, minHeight = 42.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = premiumColor),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(
            text = "Play",
            fontWeight = FontWeight.SemiBold,
            color = premiumFgColor
        )
    }
}

//================================================================
// PREVIEW FUNCTION
//================================================================
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AcmoOfferListItemPreview() {
    val sampleOffer = AcmoOffersModel(
        campaignId = "123",
        app = AppDetails(
            title = "Lords Mobile: Kingdom Wars",
            thumbnail = ""
        ),
        campaignPayout = CampaignPayout(totalPlayablePayoutConverted = 1200.50),
        currency = CurrencyInfo(adUnitCurrencyIcon = "")
    )

    AcmoOfferListItem(
        offer = sampleOffer,
        currencySales = CurrencySales(multiplier = 1.5),
        onItemTap = { },
        onButtonTap = { },
        index = 0
    )
}