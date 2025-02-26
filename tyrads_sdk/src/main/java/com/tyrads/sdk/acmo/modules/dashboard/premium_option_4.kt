package com.tyrads.sdk.acmo.modules.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.R
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.modules.dashboard.components.MyGamesButton
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.*
import com.tyrads.sdk.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun OffersScreen4(
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

    OfferCard4(
        banners = banners,
        currentIndex = currentIndex,
        targetIndex = targetIndex,
        offsetAnimation = offsetAnimation.value
    ) { direction ->
        if (!isAnimating) {
            targetIndex = when (direction) {
                SwipeDirection.LEFT -> (currentIndex + 1) % banners.size
                SwipeDirection.RIGHT -> (currentIndex - 1 + banners.size) % banners.size
            }
            isAnimating = true

            coroutineScope.launch {
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

}

@Composable
fun OfferCard4(
    banners: List<BannerData>,
    currentIndex: Int,
    targetIndex: Int,
    offsetAnimation: Float,
    onSwipe: (SwipeDirection) -> Unit
) {
    var containerWidth by remember { mutableStateOf(0) }
    var initialX by remember { mutableStateOf(0f) }

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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            PremiumHeaderSection()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(offerCard4BannerHeight)
                    .clip(RoundedCornerShape(offerCard4BannerCornerRadius))
                    .onSizeChanged { size ->
                        containerWidth = size.width
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                initialX = offset.x
                            },
                            onDragEnd = {},
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                if (abs(dragAmount) > 50) {
                                    val direction =
                                        if (dragAmount < 0) SwipeDirection.LEFT else SwipeDirection.RIGHT
                                    onSwipe(direction)
                                }
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset {
                            IntOffset(
                                (-(offsetAnimation * containerWidth)).roundToInt(),
                                0
                            )
                        }
                ) {
                    GameBanner4(banners[currentIndex])
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset {
                            IntOffset(
                                (containerWidth - (offsetAnimation * containerWidth)).roundToInt(),
                                0
                            )
                        }
                ) {
                    GameBanner4(banners[targetIndex])
                }
            }

            PaginationDots4(currentIndex, banners.size)
            MyGamesButton()
        }
    }
}

enum class SwipeDirection {
    LEFT, RIGHT
}

@Composable
fun GameBanner4(bannerData: BannerData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = bannerPaddingStart,
                end = bannerPaddingEnd,
                top = bannerPaddingTop,
                bottom = bannerPaddingBottom
            )
            .height(offerCard4BannerHeight)
            .clip(RoundedCornerShape(offerCard4BannerCornerRadius))
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
            color = Color.Transparent,
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(commonPadding),
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
                            .size(offerCard4GameIconSize)
                            .clip(RoundedCornerShape(imageCornerRadius))
                    )
                    Spacer(modifier = Modifier.width(gameInfoSpacerWidth))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bannerData.title,
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
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_coin2),
                                contentDescription = "Coin Icon",
                                modifier = Modifier.size(coinIconSize)
                            )
                            Spacer(modifier = Modifier.width(gameInfoImgSpacerWidth))
                            Text(
                                text = bannerData.points.numeral(),
                                color = Color.White,
                                fontSize = pointsFontSize
                            )
                            Text(
                                text = " ${bannerData.rewards} ${pluralStringResource(R.plurals.offers_rewards, bannerData.rewards)}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = rewardsFontSize,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = gameInfoButtonFontSize
                    )
                }
            }
        }
    }
}

@Composable
fun PaginationDots4(currentIndex: Int, totalBanners: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = paginationPaddingVertical),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalBanners) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = paginationPaddingHorizontal)
                    .size(paginationDotSize)
                    .background(
                        color = if (index == currentIndex) PrimaryBlue else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}
