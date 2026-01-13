package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.acmo.core.extensions.formatTimeRemaining
import kotlinx.coroutines.delay

@Composable
fun CountDownTimer(
    seconds: Int,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.White
) {
    var remainingSeconds by remember { mutableIntStateOf(seconds) }
    LaunchedEffect(key1 = remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else {
            remainingSeconds = 0
        }
    }
    remainingSeconds.formatTimeRemaining()?.let {
        Text(
            text = it,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color
        )
    }
}