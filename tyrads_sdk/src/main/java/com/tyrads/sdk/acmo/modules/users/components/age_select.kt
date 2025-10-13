package com.tyrads.sdk.acmo.modules.users.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.toColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AcmoComponentAgeSelector(
    onChanged: (Int) -> Unit,
    init: Int = 18,
    min: Int = 13,
    modifier: Modifier = Modifier
) {
    var selectedAge by remember { mutableStateOf(init) }
    var dragOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val itemWidth = with(density) { 60.dp.toPx() }
    val maxAge = min + 99

    LaunchedEffect(init) {
        selectedAge = init
    }

    val offsetSteps = (dragOffset / itemWidth).roundToInt()
    val displayCenterAge = (selectedAge - offsetSteps).coerceIn(min, maxAge)

    Box(
        modifier = modifier
            .height(90.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            val steps = (dragOffset / itemWidth).roundToInt()
                            val newAge = (selectedAge - steps).coerceIn(min, maxAge)
                            selectedAge = newAge
                            onChanged(newAge)
                            dragOffset = 0f
                        }
                    },
                    onDrag = { _, dragAmount ->
                        val newOffset = dragOffset + dragAmount.x
                        val steps = (newOffset / itemWidth).roundToInt()
                        val potentialAge = selectedAge - steps

                        if (potentialAge in min..maxAge) {
                            dragOffset = newOffset
                        } else {
                            val maxSteps = selectedAge - min
                            val minSteps = -(maxAge - selectedAge)
                            val limitedSteps = steps.coerceIn(minSteps, maxSteps)
                            dragOffset = limitedSteps * itemWidth + (newOffset - steps * itemWidth) * 0.2f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val farLeftAge = displayCenterAge - 2
            AgeButton(farLeftAge, min, maxAge, selectedAge, alpha = 0.3f, fontSize = 16.sp) {
                coroutineScope.launch {
                    selectedAge = farLeftAge
                    onChanged(farLeftAge)
                    dragOffset = 0f
                }
            }

            val leftAge = displayCenterAge - 1
            AgeButton(leftAge, min, maxAge, selectedAge, alpha = 0.54f, fontSize = 22.sp) {
                coroutineScope.launch {
                    selectedAge = leftAge
                    onChanged(leftAge)
                    dragOffset = 0f
                }
            }

            // ✅ Animated Center Box
            val isSelected = displayCenterAge == selectedAge && dragOffset.roundToInt() == 0
            val animatedBg by animateColorAsState(
                targetValue = if (isSelected) Color(0xFFF6F6F6) else Color.Transparent,
                label = "backgroundColor"
            )
            val animatedScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                label = "scaleAnim"
            )
            val animatedTextColor by animateColorAsState(
                targetValue = if (isSelected)
                    (Tyrads.getInstance().mainColor?.toColor() ?: Color(0xFF2CB388))
                else
                    Color.Black.copy(alpha = 0.7f),
                label = "textColor"
            )

            Box(
                modifier = Modifier
                    .background(animatedBg, RoundedCornerShape(12.dp))
                    .width(100.dp)
                    .height(70.dp)
                    .scale(animatedScale),
                contentAlignment = Alignment.Center
            ) {
                if (displayCenterAge in min..maxAge) {
                    Text(
                        text = displayCenterAge.toString(),
                        style = TextStyle(
                            color = animatedTextColor,
                            fontSize = 32.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }

            val rightAge = displayCenterAge + 1
            AgeButton(rightAge, min, maxAge, selectedAge, alpha = 0.54f, fontSize = 22.sp) {
                coroutineScope.launch {
                    selectedAge = rightAge
                    onChanged(rightAge)
                    dragOffset = 0f
                }
            }

            val farRightAge = displayCenterAge + 2
            AgeButton(farRightAge, min, maxAge, selectedAge, alpha = 0.3f, fontSize = 16.sp) {
                coroutineScope.launch {
                    selectedAge = farRightAge
                    onChanged(farRightAge)
                    dragOffset = 0f
                }
            }
        }
    }
}

@Composable
private fun AgeButton(
    age: Int,
    min: Int,
    max: Int,
    selectedAge: Int,
    alpha: Float,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    val isInRange = age in min..max
    TextButton(
        onClick = onClick,
        enabled = isInRange,
        contentPadding = PaddingValues(if (fontSize.value > 20f) 8.dp else 5.dp)
    ) {
        Text(
            text = if (isInRange) age.toString() else "",
            style = TextStyle(
                color = Color.Black.copy(alpha = alpha),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
