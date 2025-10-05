package com.tyrads.sdk.acmo.modules.users.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    LaunchedEffect(init) {
        selectedAge = init
    }

    val centerIndex = selectedAge - min
    val offsetSteps = (dragOffset / with(density) { 60.dp.toPx() }).toInt()
    val virtualCenter = (centerIndex - offsetSteps).coerceIn(0, 99)

    Box(
        modifier = modifier
            .height(90.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val snapSteps = (dragOffset / with(density) { 60.dp.toPx() }).toInt()
                        val newAge = (selectedAge - snapSteps).coerceIn(min, min + 99)

                        coroutineScope.launch {
                            selectedAge = newAge
                            onChanged(newAge)
                            dragOffset = 0f
                        }
                    }
                ) { _, dragAmount ->
                    dragOffset += dragAmount.x
                    // Limit drag range
                    val maxOffset = (99 - (selectedAge - min)) * with(density) { 60.dp.toPx() }
                    val minOffset = -(selectedAge - min) * with(density) { 60.dp.toPx() }
                    dragOffset = dragOffset.coerceIn(minOffset, maxOffset)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Far left age number (smallest, least visible)
            val farLeftAge = min + virtualCenter - 2
            TextButton(
                onClick = {
                    if (farLeftAge >= min && farLeftAge != selectedAge) {
                        selectedAge = farLeftAge
                        onChanged(farLeftAge)
                        dragOffset = 0f
                    }
                },
                enabled = farLeftAge >= min && farLeftAge <= min + 99,
                contentPadding = PaddingValues(5.dp)
            ) {
                Text(
                    text = if (farLeftAge >= min && farLeftAge <= min + 99) farLeftAge.toString() else "",
                    style = TextStyle(
                        color = Color.Black.copy(alpha = 0.3f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Previous age number (medium size)
            val leftAge = min + virtualCenter - 1
            TextButton(
                onClick = {
                    if (leftAge >= min && leftAge != selectedAge) {
                        selectedAge = leftAge
                        onChanged(leftAge)
                        dragOffset = 0f
                    }
                },
                enabled = leftAge >= min && leftAge <= min + 99,
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = if (leftAge >= min && leftAge <= min + 99) leftAge.toString() else "",
                    style = TextStyle(
                        color = Color.Black.copy(alpha = 0.54f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Current age display with background (largest)
            val currentAge = min + virtualCenter
            Box(
                modifier = Modifier
                    .background(
                        color = if (currentAge == selectedAge) Color(0xFFF6F6F6) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .width(100.dp)
                    .height(70.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentAge >= min && currentAge <= min + 99) currentAge.toString() else "",
                    style = TextStyle(
                        color = if (currentAge == selectedAge) (Tyrads.getInstance().mainColor?.toColor() ?: Color( AcmoConfig.SECONDARY_COLOR)) else Color.Black.copy(alpha = 0.7f),
                        fontSize = 32.sp,
                        fontWeight = if (currentAge == selectedAge) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }

            // Next age number (medium size)
            val rightAge = min + virtualCenter + 1
            TextButton(
                onClick = {
                    if (rightAge <= min + 99 && rightAge != selectedAge) {
                        selectedAge = rightAge
                        onChanged(rightAge)
                        dragOffset = 0f
                    }
                },
                enabled = rightAge >= min && rightAge <= min + 99,
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = if (rightAge >= min && rightAge <= min + 99) rightAge.toString() else "",
                    style = TextStyle(
                        color = Color.Black.copy(alpha = 0.54f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Far right age number (smallest, least visible)
            val farRightAge = min + virtualCenter + 2
            TextButton(
                onClick = {
                    if (farRightAge <= min + 99 && farRightAge != selectedAge) {
                        selectedAge = farRightAge
                        onChanged(farRightAge)
                        dragOffset = 0f
                    }
                },
                enabled = farRightAge >= min && farRightAge <= min + 99,
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(
                    text = if (farRightAge >= min && farRightAge <= min + 99) farRightAge.toString() else "",
                    style = TextStyle(
                        color = Color.Black.copy(alpha = 0.3f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
