package com.tyrads.sdk.acmo.modules.users.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.toColor
import kotlinx.coroutines.flow.filter

@Composable
fun AcmoHorizontalNumberPicker(
    modifier: Modifier = Modifier,
    initialValue: Int = 18,
    minValue: Int = 13,
    maxValue: Int = 109,
    onValueChange: (Int) -> Unit = {}
) {
    var selectedValue by remember { mutableIntStateOf(initialValue) }
    val selectedItemWidth = 100.dp
    val normalItemWidth = 50.dp
    val itemWidth = 100.dp
    val itemHeight = 65.dp
    val numbers = remember { (minValue..maxValue).toList() }
    var initialized by remember { mutableIntStateOf(0) }

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialValue - minValue
    )

    val centeredItemIndex by remember {
        derivedStateOf {
            calculateCenteredItemIndex(scrollState, initialValue - minValue)
        }
    }

    LaunchedEffect(Unit) {
        selectedValue = initialValue
        onValueChange(initialValue)
        kotlinx.coroutines.delay(1000)
        scrollState.scrollToItem(initialValue - minValue)
        initialized = 1
    }

    LaunchedEffect(centeredItemIndex) {
        if (initialized == 0) return@LaunchedEffect
        val newValue = numbers.getOrNull(centeredItemIndex) ?: initialValue
        if (newValue != selectedValue) {
            selectedValue = newValue
            onValueChange(newValue)
        }

    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .filter { !it }
            .collect {
                if (initialized == 0) return@collect
                val newValue = numbers.getOrNull(centeredItemIndex) ?: initialValue
                if (newValue != selectedValue) {
                    selectedValue = newValue
                    onValueChange(newValue)
                }
            }
    }

    Box(
        modifier = modifier.height(itemHeight),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            state = scrollState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollState),
            contentPadding = PaddingValues(horizontal = itemWidth * 2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                count = numbers.size,
                key = { index -> numbers[index] }
            ) { index ->
                val number = numbers[index]
                val isSelected = number == selectedValue
                val itemWidth = if (isSelected) selectedItemWidth else normalItemWidth

                NumberItem(
                    number = number,
                    isSelected = isSelected,
                    itemWidth = itemWidth,
                    itemHeight = itemHeight
                )
            }
        }
    }
}

private fun calculateCenteredItemIndex(
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    fallbackIndex: Int
): Int {
    val layoutInfo = scrollState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo

    if (visibleItems.isEmpty()) return fallbackIndex

    val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2

    return visibleItems.minByOrNull { item ->
        val itemCenter = item.offset + item.size / 2
        kotlin.math.abs(itemCenter - viewportCenter)
    }?.index ?: fallbackIndex
}

@Composable
private fun NumberItem(
    number: Int,
    isSelected: Boolean,
    itemWidth: Dp,
    itemHeight: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(itemWidth)
            .height(itemHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) "#F6F6F6".toColor() else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = if (isSelected) 30.sp else 17.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.W500,
            color = if (isSelected) (Tyrads.getInstance().mainColor?.toColor()
                ?: Color.Blue) else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
