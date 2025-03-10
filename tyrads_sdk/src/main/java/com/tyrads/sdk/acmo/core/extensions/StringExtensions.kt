package com.tyrads.sdk.acmo.core.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun String.toColor(): Color {
    return Color(android.graphics.Color.parseColor(this))
}

fun Color.contrastingColor(): Color {
    return if (this.luminance() > 0.6) {
        Color.Black
    } else {
        Color.White
    }
}