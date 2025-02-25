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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.R
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.*
import com.tyrads.sdk.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class PremiumActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OffersScreen()
        }
    }
}

@Composable
fun OffersScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightGrayColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = offersScreenPaddingTop),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SlidingBannerSystem2()
        }
    }
}

@Composable
fun SlidingBannerSystem2() {
    var currentIndex by remember { mutableStateOf(0) }
    var targetIndex by remember { mutableStateOf(0) }
    var banners by remember { mutableStateOf<List<BannerData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val networkCommons =
        remember { NetworkCommons() }// Instance Created for NetworkCommons using remember

    LaunchedEffect(Unit) {
        networkCommons.fetchCampaigns(
            onSuccess = {
                banners = it
                isLoading = false
            },
            onError = {
                error = it.message
                isLoading = false
            }
        )
    }
    when {
        isLoading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(loaderSize),
                color = MaterialTheme.colorScheme.primary
            )
        }

        error != null -> {
            Text(
                text = "Error: $error",
                color = RedColor,
                modifier = Modifier.padding(errorPadding)
            )
        }

        banners.isEmpty() -> {
            Text(
                text = "No campaigns available",
                modifier = Modifier.padding(noCampaignPadding)
            )
        }

        else -> {
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
    }
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
        ) {
            HeaderSection2()

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
            MyGamesButton2()
        }
    }
}

@Composable
fun HeaderSection2() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = headerPaddingStart,
                end = headerPaddingEnd,
                top = headerPaddingTop,
                bottom = headerPaddingBottom
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_star_new),
                contentDescription = "Star",
                modifier = Modifier.size(starIconSize)
            )
            Spacer(modifier = Modifier.width(headerTextSpacing))
            Text(
                text = "Suggested Offers",
                fontSize = headerFontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { }
        ) {
            Text(
                text = "More Offers",
                color = PrimaryBlue,
                fontSize = moreOffersFontSize,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(headerIconSpacing))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Arrow",
                modifier = Modifier.size(moreOffersIconSize),
                tint = PrimaryBlue
            )
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
                            text = bannerData.points,
                            color = WhiteColor,
                            fontSize = pointsFontSize,
                        )
                        Text(
                            text = "  ${bannerData.rewards}",
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
                    text = "Play Now",
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


@Composable
fun MyGamesButton2() {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = myGamesButtonPadding,
                end = myGamesButtonPadding,
//                top =  myGamesButtonPadding,
                bottom = myGamesButtonPadding
            )
            .height(myGamesButtonHeight),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        shape = RoundedCornerShape(myGamesButtonCornerRadius)
    ) {
        Text(
            text = "My Games",
            fontSize = myGamesButtonFontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OfferCardPreview() {
    OffersScreen()

}