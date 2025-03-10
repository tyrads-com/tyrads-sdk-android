package com.tyrads.sdk.acmo.modules.dashboard.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.modules.input_models.autoScrollDelay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoScrollPagerWithIndicators(
    totalPages: Int,
    delayInMillis: Long = autoScrollDelay,
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 1,
        initialPageOffsetFraction = 0f,
        pageCount = { totalPages },
    )
    LaunchedEffect (Unit){
        while (true){
            delay(delayInMillis)
            val nextBanner = if (pagerState.currentPage + 1 == totalPages) {
                0
            } else {
                pagerState.currentPage + 1
            }
            coroutineScope.launch {
                pagerState.animateScrollToPage(nextBanner)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) {
            content(it)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            repeat(totalPages){
                val color = if(pagerState.currentPage == it){
                    Tyrads.getInstance().premiumColor.toColor()
                }else{
                    Color.LightGray
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}