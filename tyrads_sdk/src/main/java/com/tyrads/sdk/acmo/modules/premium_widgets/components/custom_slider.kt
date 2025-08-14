package com.tyrads.sdk.acmo.modules.premium_widgets.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun AcmoCarouselSlider(
    modifier: Modifier = Modifier,
    itemCount: Int,
    itemBuilder: @Composable (Int) -> Unit,
    autoPlayIntervalMs: Long = 5000L,
    showIndicator: Boolean = true,
    infiniteScroll: Boolean = true,
    indicatorActiveColor: Color? = null,
    indicatorInactiveColor: Color? = null,
    indicatorSize: Dp = 8.dp,
    indicatorSpacing: Dp = 4.dp,
    viewportFraction: Float = 0.85f,
    scaleFactor: Float = 0.94f,
    initialPage: Int = 0,
    onPageChanged: ((Int) -> Unit)? = null
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val pagerState = rememberPagerState(
        initialPage = if (infiniteScroll && itemCount > 0) {
            (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % itemCount) + (initialPage % itemCount)
        } else {
            initialPage % itemCount
        },
        pageCount = { if (infiniteScroll) Int.MAX_VALUE else itemCount }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(itemCount, autoPlayIntervalMs) {
        if (itemCount > 1 && autoPlayIntervalMs > 0) {
            while (true) {
                delay(autoPlayIntervalMs)
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    val currentRealIndex = if (infiniteScroll && itemCount > 0) {
        pagerState.currentPage % itemCount
    } else {
        pagerState.currentPage
    }

    LaunchedEffect(currentRealIndex) {
        onPageChanged?.invoke(currentRealIndex)
    }

    val horizontalPadding = (screenWidth * (1 - viewportFraction) / 2)

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 4.dp,
            contentPadding = PaddingValues(horizontal = horizontalPadding)
        ) { page ->
            val realIndex = if (infiniteScroll && itemCount > 0) {
                ((page % itemCount) + itemCount) % itemCount
            } else {
                page
            }

            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val scale = lerp(
                start = scaleFactor,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                itemBuilder(realIndex)
            }
        }

        if (showIndicator && itemCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(itemCount) { index ->
                        val isActive = index == currentRealIndex
                        val animatedScale by animateFloatAsState(
                            targetValue = if (isActive) 1.2f else 1.0f,
                            animationSpec = tween(durationMillis = 300),
                            label = "indicator_scale"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = indicatorSpacing)
                                .size(indicatorSize)
                                .scale(animatedScale)
                                .clip(CircleShape)
                                .background(
                                    color = if (isActive) {
                                        indicatorActiveColor ?: MaterialTheme.colorScheme.primary
                                    } else {
                                        indicatorInactiveColor ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}