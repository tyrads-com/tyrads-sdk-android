package com.tyrads.sdk.acmo.modules.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
import com.tyrads.sdk.acmo.modules.input_models.bannerHeight
import com.tyrads.sdk.acmo.modules.input_models.bannerStarIconSize
import com.tyrads.sdk.acmo.modules.input_models.bannerSurfacePadding
import com.tyrads.sdk.acmo.modules.input_models.bannerSurfaceSize
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
import com.tyrads.sdk.ui.theme.TransparentColor
import com.tyrads.sdk.ui.theme.WhiteColor


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OffersScreen(
    banners: List<BannerData>
) {
    AutoScrollPagerWithIndicators(
        banners.size
    ) {
       Column(
           Modifier.clickable { Tyrads.getInstance()
               .showOffers(route = "campaign-details", campaignID = banners[it].campaignId) }
       ) {
           GameBanner(banners[it])
           GameInfoSection(banners[it])
       }
    }
}

@Composable
fun GameBanner(bannerData: BannerData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(bannerData.fileUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Game Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .size(bannerSurfaceSize)
                .padding(bannerSurfacePadding)
                .align(Alignment.TopStart)
        ) {
            Image(
                painter = painterResource(id = R.drawable.premium_star),
                contentDescription = "Star",
                modifier = Modifier.size(bannerStarIconSize).align(Alignment.Center),
                colorFilter = ColorFilter.tint(Tyrads.getInstance().premiumColor.toColor())
            )
            Text(
                text="$",
                color= Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameInfoSection(bannerData: BannerData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tyrads.getInstance().premiumColor.toColor())
            .padding(gameInfoPadding)
            .wrapContentHeight()
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
                        text = bannerData.title,
                        color = WhiteColor,
                        fontSize = gameTextFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = gameInfoPaddingBottom)
                            .basicMarquee(),
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
                            color = WhiteColor,
                            fontSize = pointsFontSize,
                        )
                        Text(
                            text = "  ${bannerData.rewards} ${pluralStringResource(R.plurals.offers_rewards, bannerData.rewards)}",
                            color = WhiteColor,
                            fontSize = rewardsFontSize,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(gameInfoSpacerWidth))
            Button(
                onClick = { Tyrads.getInstance()
                    .showOffers(route = "campaign-details", campaignID = bannerData.campaignId) },
                colors = ButtonDefaults.buttonColors(containerColor = WhiteColor),
                shape = RoundedCornerShape(playButtonCornerRadius),
                contentPadding = PaddingValues(
                    horizontal = gameInfoButtonPaddingHorizontal,
                    vertical = gameInfoButtonPaddingVertical
                ),
                modifier = Modifier.height(playButtonHeight)
            ) {
                Text(
                    text = stringResource(id = R.string.dashboard_play_button),
                    color = Tyrads.getInstance().premiumColor.toColor(),
                    fontWeight = FontWeight.Bold,
                    fontSize = gameInfoButtonFontSize
                )
            }
        }
    }
}