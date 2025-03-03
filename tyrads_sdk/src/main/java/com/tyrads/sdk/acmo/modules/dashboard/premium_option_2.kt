package com.tyrads.sdk.acmo.modules.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.R
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.modules.dashboard.components.AutoScrollPagerWithIndicators
import com.tyrads.sdk.acmo.modules.dashboard.components.MyGamesButton
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.*
import com.tyrads.sdk.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OffersScreen(
    banners: List<BannerData>
) {
    AutoScrollPagerWithIndicators(
        banners.size
    ) {
       Column {
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
        Surface(
            color = TransparentColor,
            modifier = Modifier
                .size(bannerSurfaceSize)
                .padding(bannerSurfacePadding)
                .align(Alignment.TopStart)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_dollar_star),
                contentDescription = "Star",
                modifier = Modifier.size(bannerStarIconSize)
            )
        }
    }
}

@Composable
fun GameInfoSection(bannerData: BannerData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlue)
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
                        text = bannerData.title,  // Changed from creativePackName
                        color = WhiteColor,
                        fontSize = gameTextFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = gameInfoPaddingBottom),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = gameInfoPaddingTop)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_coin2),
                            contentDescription = "Coin Icon",
                            modifier = Modifier.size(coinIconSize)
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
                onClick = { },
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
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = gameInfoButtonFontSize
                )
            }
        }
    }
}