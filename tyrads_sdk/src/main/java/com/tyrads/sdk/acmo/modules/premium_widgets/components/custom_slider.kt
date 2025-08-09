package com.tyrads.sdk.acmo.modules.premium_widgets.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AcmoCarouselSlider(
    modifier: Modifier = Modifier,
    itemCount: Int,
    itemBuilder: @Composable (Int) -> Unit,
    autoPlayIntervalMs: Long = 5000L, // Duration(seconds: 5) equivalent
    showIndicator: Boolean = true,
    infiniteScroll: Boolean = true,
    indicatorActiveColor: Color? = null,
    indicatorInactiveColor: Color? = null,
    indicatorSize: Dp = 8.dp,
    indicatorSpacing: Dp = 4.dp,
    viewportFraction: Float = 0.9f,
    scaleFactor: Float = 0.94f,
    initialPage: Int = 0,
    onPageChanged: ((Int) -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val pagerState = rememberPagerState(
        initialPage = if (infiniteScroll && itemCount > 0) {
            (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % itemCount) + (initialPage % itemCount)
        } else {
            initialPage % itemCount
        },
        pageCount = { if (infiniteScroll) Int.MAX_VALUE else itemCount }
    )

    val coroutineScope = rememberCoroutineScope()

    // Auto-play effect equivalent to Timer.periodic
    LaunchedEffect(itemCount, autoPlayIntervalMs) {
        if (itemCount > 1 && autoPlayIntervalMs > 0) {
            while (true) {
                delay(autoPlayIntervalMs)
                if (itemCount > 0) {
                    coroutineScope.launch {
                        val nextPage = pagerState.currentPage + 1
                        pagerState.animateScrollToPage(
                            page = nextPage,
                            animationSpec = tween(
                                durationMillis = 1000, // Duration(milliseconds: 1000)
                                easing = FastOutSlowInEasing // Curves.easeInOut equivalent
                            )
                        )
                    }
                }
            }
        }
    }

    // Track current index for callbacks
    val currentRealIndex = if (infiniteScroll && itemCount > 0) {
        pagerState.currentPage % itemCount
    } else {
        pagerState.currentPage
    }

    // Call onPageChanged when index changes
    LaunchedEffect(currentRealIndex) {
        onPageChanged?.invoke(currentRealIndex)
    }

    Box(modifier = modifier) {
        // HorizontalPager equivalent to PageView.builder
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp
        ) { page ->
            val realIndex = if (infiniteScroll && itemCount > 0) {
                ((page % itemCount) + itemCount) % itemCount // _getLoopedIndex equivalent
            } else {
                page
            }

            // AnimatedBuilder equivalent - calculate scale based on page offset
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val scale = calculateScale(pageOffset.absoluteValue, scaleFactor)
            val transformedScale = easeOutTransform(scale)

            // Center equivalent with SizedBox and Transform.scale
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            width = (transformedScale * screenWidth.value * viewportFraction).dp,
                            height = (transformedScale * screenHeight.value * 0.8f).dp
                        )
                        .scale(scale)
                ) {
                    itemBuilder(realIndex)
                }
            }
        }

        // Indicator equivalent
        if (showIndicator && itemCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(itemCount) { index ->
                        val isActive = index == currentRealIndex

                        // TweenAnimationBuilder equivalent
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

// Helper function equivalent to Curves.easeOut.transform
private fun easeOutTransform(value: Float): Float {
    return 1.0f - (1.0f - value) * (1.0f - value)
}

// Helper function to calculate scale based on page offset
private fun calculateScale(pageOffset: Float, scaleFactor: Float): Float {
    val clampedOffset = min(pageOffset, 1.0f)
    val transformedOffset = easeOutTransform(clampedOffset)
    return scaleFactor + (1 - scaleFactor) * (1 - transformedOffset)
}

// Sample data class for preview
data class CarouselItem(
    val id: Int,
    val title: String,
    val color: Color
)

@Preview(showBackground = true)
@Composable
fun AcmoCarouselSliderPreview() {
    val sampleItems = listOf(
        CarouselItem(1, "Item 1", Color.Red),
        CarouselItem(2, "Item 2", Color.Green),
        CarouselItem(3, "Item 3", Color.Blue),
        CarouselItem(4, "Item 4", Color.Yellow),
        CarouselItem(5, "Item 5", Color.Magenta)
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            AcmoCarouselSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                itemCount = sampleItems.size,
                itemBuilder = { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = sampleItems[index].color,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sampleItems[index].title,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                autoPlayIntervalMs = 3000L,
                showIndicator = true,
                infiniteScroll = true,
                viewportFraction = 0.8f,
                scaleFactor = 0.9f,
                onPageChanged = { index ->
                    println("Page changed to: $index")
                }
            )
        }
    }
}