package com.tyrads.sdk.acmo.core.extensions

import java.util.Locale

fun Double.numeral(): String {
    return when {
        this >= 1_000_000_000_000 -> String.format(Locale.US, "%.2fT", this / 1_000_000_000_000).removeTrailingZeros()
        this >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", this / 1_000_000_000).removeTrailingZeros()
        this >= 1_000_000 -> String.format(Locale.US, "%.2fM", this / 1_000_000).removeTrailingZeros()
        this >= 1_000 -> String.format(Locale.US, "%.2fK", this / 1_000).removeTrailingZeros()
        else -> this.toString()
    }
}

fun String.removeTrailingZeros(): String {
    return this.replace(Regex("([.]\\d*[1-9])0+$"), "$1").replace(Regex("[.]0+$"), "").replace(Regex("([.]00)"), "")
}

fun Int?.formatTimeRemaining(): String? {
    if (this == null || this <= 0) return null

    val days =  this/86400
    val hours = (this % 86400) / 3600
    val minutes = (this % 3600) / 60
    val secs = this % 60

    fun pad(value: Int): String = value.toString().padStart(2, '0')

    return if(days > 0) {
        "${pad(days)}d ${pad(hours)}h"
    } else {
        "${pad(hours)}:${pad(minutes)}:${pad(secs)}"
    }
}