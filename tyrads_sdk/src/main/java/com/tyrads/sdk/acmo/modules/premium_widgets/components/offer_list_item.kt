package com.tyrads.sdk.acmo.modules.premium_widgets.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.core.services.LocalizationService
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersModel
import com.tyrads.sdk.acmo.modules.input_models.CurrencySales
import kotlinx.coroutines.launch

@Composable
fun AcmoOfferListItem(
    modifier: Modifier = Modifier,
    offer: AcmoOffersModel,
    currencySales: CurrencySales?,
    onItemTap: () -> Unit,
    onButtonTap: suspend () -> Unit,
    index: Int,
    loadingIndex: Int?,
    onLoadingIndexChange: (Int?) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val premiumColor = Tyrads.getInstance().premiumColor.toColor()
    val premiumFgColor = MaterialTheme.colorScheme.onPrimary
    val localizationService = LocalizationService.getInstance()

    // Loading state calculations
    val isLoading = loadingIndex == index
    val anyLoading = loadingIndex != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !anyLoading,
                onClick = onItemTap
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(54.dp)) {
            OfferThumbnail(
                thumbnailUrl = offer.app.thumbnail,
                title = offer.app.title
            )
            RankIcon(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-10).dp, y = (-8).dp),
                index = index
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        OfferDetails(
            modifier = Modifier.weight(1f),
            offer = offer,
            currencySales = currencySales,
            premiumColor = premiumColor,
            localizationService = localizationService
        )

        Spacer(modifier = Modifier.width(8.dp))

        PlayButton(
            onButtonTap = {
                coroutineScope.launch {
                    onLoadingIndexChange(index)
                    try {
                        onButtonTap()
                    } finally {
                        onLoadingIndexChange(null)
                    }
                }
            },
            premiumColor = premiumColor,
            premiumFgColor = premiumFgColor,
            isLoading = isLoading,
            anyLoading = anyLoading,
            localizationService = localizationService
        )
    }
}

@Composable
private fun RankIcon(modifier: Modifier, index: Int) {
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
            modifier = modifier
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
    premiumColor: Color,
    localizationService: LocalizationService
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        currencySales?.let {
            BonusLabel(
                multiplier = it.multiplier ?: 1.0,
                premiumColor = premiumColor,
                localizationService = localizationService
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
private fun BonusLabel(
    multiplier: Double,
    premiumColor: Color,
    localizationService: LocalizationService
) {
    Box(
        modifier = Modifier
            .background(
                color = premiumColor.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = localizationService.translate(
                "data.widget.bonus.multiplier",
                mapOf("multiplier" to multiplier)
            ),
            color = premiumColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun OfferPayout(offer: AcmoOffersModel, currencySales: CurrencySales?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        currencySales?.let {
            Text(
                text = offer.campaignPayout.totalPlayablePayoutConverted.numeral(),
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
            text = (offer.campaignPayout.totalPlayablePayoutConverted * (currencySales?.multiplier
                ?: 1.0)).numeral(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlayButton(
    onButtonTap: () -> Unit,
    premiumColor: Color,
    premiumFgColor: Color,
    isLoading: Boolean,
    anyLoading: Boolean,
    localizationService: LocalizationService
) {
    Button(
        onClick = onButtonTap,
        enabled = !anyLoading,
        modifier = Modifier.sizeIn(minWidth = 75.dp, minHeight = 42.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (anyLoading) Color(0xFFE0E2E7) else premiumColor,
            disabledContainerColor = Color(0xFFE0E2E7)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.2.dp,
                    color = Color(0xFFA3A9B6)
                )
            }

            Text(
                text = localizationService.translate("data.widget.button.play"),
                fontWeight = FontWeight.SemiBold,
                color = if (anyLoading) Color(0xFFA3A9B6) else premiumFgColor
            )
        }
    }
}