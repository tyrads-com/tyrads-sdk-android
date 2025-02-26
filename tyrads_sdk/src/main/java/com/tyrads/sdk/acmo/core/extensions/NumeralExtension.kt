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