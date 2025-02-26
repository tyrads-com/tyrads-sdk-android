package com.tyrads.sdk.acmo.modules.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import com.tyrads.sdk.acmo.modules.dashboard.components.MyGamesButton
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.*
import com.tyrads.sdk.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun OffersScreen(
    banners: List<BannerData>
) {
    var currentIndex by remember { mutableStateOf(0) }
    var targetIndex by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val offsetAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(autoScrollDelay)
            if (!isAnimating) {
                targetIndex = (currentIndex + 1) % banners.size
                isAnimating = true

                offsetAnimation.snapTo(0f)
                offsetAnimation.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = LinearOutSlowInEasing
                    )
                )
                currentIndex = targetIndex
                isAnimating = false
            }
        }
    }
    OfferCard(
        banners = banners,
        currentIndex = currentIndex,
        targetIndex = targetIndex,
        offsetAnimation = offsetAnimation.value,
        onSwipe = { direction ->
            if (!isAnimating) {
                coroutineScope.launch {
                    isAnimating = true
                    targetIndex = when (direction) {
                        SwipeDirection2.LEFT -> (currentIndex + 1) % banners.size
                        SwipeDirection2.RIGHT -> (currentIndex - 1 + banners.size) % banners.size
                    }

                    offsetAnimation.snapTo(0f)
                    offsetAnimation.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = animationDuration,
                            easing = LinearOutSlowInEasing
                        )
                    )
                    currentIndex = targetIndex
                    isAnimating = false
                }
            }
        }
    )
}

enum class SwipeDirection2 {
    LEFT, RIGHT
}

@Composable
fun OfferCard(
    banners: List<BannerData>,
    currentIndex: Int,
    targetIndex: Int,
    offsetAnimation: Float,
    onSwipe: (SwipeDirection2) -> Unit
) {
    var containerWidth by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                horizontal = cardPaddingHorizontal,
                vertical = cardPaddingVertical
            ),
        shape = RoundedCornerShape(
            topStart = cardCornerTopStart,
            topEnd = cardCornerTopEnd,
            bottomEnd = cardCornerBottomEnd,
            bottomStart = cardCornerBottomStart
        ),
        colors = CardDefaults.cardColors(containerColor = WhiteColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
        ) {
            PremiumHeaderSection()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        containerWidth = size.width
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            when {
                                dragAmount < -50 -> onSwipe(SwipeDirection2.LEFT)
                                dragAmount > 50 -> onSwipe(SwipeDirection2.RIGHT)
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            IntOffset(
                                (-(offsetAnimation * containerWidth)).roundToInt(),
                                0
                            )
                        }
                ) {
                    GameBanner(banners[currentIndex])
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            IntOffset(
                                (containerWidth - (offsetAnimation * containerWidth)).roundToInt(),
                                0
                            )
                        }
                ) {
                    GameBanner(banners[targetIndex])
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            IntOffset(
                                (-(offsetAnimation * containerWidth)).roundToInt(),
                                0
                            )
                        }
                ) {
                    GameInfoSection(banners[currentIndex])
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            IntOffset(
                                (containerWidth - (offsetAnimation * containerWidth)).roundToInt(),
                                0
                            )
                        }
                ) {
                    GameInfoSection(banners[targetIndex])
                }
            }
            PaginationDots(currentIndex, banners.size)
            MyGamesButton()
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

@Composable
fun PaginationDots(currentIndex: Int, totalItems: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = paginationPaddingVertical),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalItems) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = paginationPaddingHorizontal)
                    .size(paginationDotSize)
                    .background(
                        color = if (index == currentIndex) PaginationActiveDot else PaginationInactiveDot,
                        shape = CircleShape
                    )
            )
        }
    }
}
