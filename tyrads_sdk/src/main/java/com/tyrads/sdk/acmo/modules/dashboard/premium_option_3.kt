package com.tyrads.sdk.acmo.modules.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.modules.dashboard.components.AutoScrollPagerWithIndicators
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.coinIconSize
import com.tyrads.sdk.acmo.modules.input_models.gameInfoButtonFontSize
import com.tyrads.sdk.acmo.modules.input_models.gameInfoButtonPaddingHorizontal
import com.tyrads.sdk.acmo.modules.input_models.gameInfoButtonPaddingVertical
import com.tyrads.sdk.acmo.modules.input_models.gameInfoImgSpacerWidth
import com.tyrads.sdk.acmo.modules.input_models.gameInfoPadding
import com.tyrads.sdk.acmo.modules.input_models.gameInfoPaddingBottom
import com.tyrads.sdk.acmo.modules.input_models.gameInfoPaddingTop
import com.tyrads.sdk.acmo.modules.input_models.gameInfoSpacerWidth
import com.tyrads.sdk.acmo.modules.input_models.gameTextFontSize
import com.tyrads.sdk.acmo.modules.input_models.imageCornerRadius
import com.tyrads.sdk.acmo.modules.input_models.imageSize
import com.tyrads.sdk.acmo.modules.input_models.playButtonCornerRadius
import com.tyrads.sdk.acmo.modules.input_models.playButtonHeight
import com.tyrads.sdk.acmo.modules.input_models.pointsFontSize
import com.tyrads.sdk.acmo.modules.input_models.rewardsFontSize

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OffersScreen3(
    banners: List<BannerData>
) {
    AutoScrollPagerWithIndicators(
        banners.size,
    ) {
        GameInfoSection3(
            bannerData = banners[it]
        )
    }
}

@Composable
fun GameInfoSection3(bannerData: BannerData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tyrads.getInstance().premiumColor.toColor())
            .padding(gameInfoPadding)
            .wrapContentHeight()
            .clickable {
                Tyrads.getInstance().showOffers(route = "campaign-details", campaignID = bannerData.campaignId)
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(bannerData.thumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Game Icon",
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(imageCornerRadius))
                )
                Spacer(modifier = Modifier.width(gameInfoSpacerWidth))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = bannerData.creativePackName,
                        color = Color.White,
                        fontSize = gameTextFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = gameInfoPaddingBottom)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = gameInfoPaddingTop)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(bannerData.currency.adUnitCurrencyIcon)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Game Icon",
                            modifier = Modifier
                                .size(coinIconSize)
                        )
                        Spacer(modifier = Modifier.width(gameInfoImgSpacerWidth))
                        Text(
                            text = bannerData.points.numeral(),
                            color = Color.White,
                            fontSize = pointsFontSize
                        )
                        Text(
                            text = "  ${bannerData.rewards} ${pluralStringResource(R.plurals.offers_rewards, bannerData.rewards)}",
                            color = Color.White,
                            fontSize = rewardsFontSize
                        )
                    }
                }
            }
            Button(
                onClick = {
                    Tyrads.getInstance().showOffers(route = "campaign-details", campaignID = bannerData.campaignId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(playButtonCornerRadius),
                contentPadding = PaddingValues(
                    horizontal = gameInfoButtonPaddingHorizontal,
                    vertical = gameInfoButtonPaddingVertical
                ),
                modifier = Modifier
                    .height(playButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_play_button),
                    color = Tyrads.getInstance().premiumColor.toColor(),
                    fontWeight = FontWeight.Bold,
                    fontSize = gameInfoButtonFontSize
                )
            }
        }
    }
}


