package com.tyrads.sdk.acmo.core.utils

import java.text.SimpleDateFormat
import java.util.*

fun getSystemClockInfo(): Map<String, String> {
    val systemTime = getCurrentFormattedTime()
    val timeZone = getCurrentTimeZone()
    val locale = getCurrentLocale()

    return mapOf(
        "system_time" to systemTime,
        "time_zone" to timeZone,
        "locale" to locale
    )
}

private fun getCurrentFormattedTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDate = Date()
    return sdf.format(currentDate)
}

private fun getCurrentSystemTime(): Long {
    val currentTimeMillis = System.currentTimeMillis()
    return currentTimeMillis
}

private fun getCurrentTimeZone(): String {
    val timeZone = TimeZone.getDefault()
    return timeZone.id.toString()
}

private fun getCurrentLocale(): String {
    val locale = Locale.getDefault()
    return locale.toString()
}
