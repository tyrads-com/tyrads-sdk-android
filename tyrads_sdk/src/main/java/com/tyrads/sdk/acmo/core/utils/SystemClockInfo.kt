package com.tyrads.sdk.acmo.core.utils

import java.text.SimpleDateFormat
import java.util.*

fun getSystemClockInfo(): Map<String, Any> {
    val systemTime = getCurrentFormattedTime()
    val timeZone = getCurrentTimeZone()
    val timeZoneName = getCurrentTimeZoneName()
    val timeZoneOffset = getCurrentTimeZoneOffset()
    val locale = getCurrentLocale()

    return mapOf(
        "system_time" to systemTime,
        "time_zone" to timeZone,
        "time_zone_name" to timeZoneName,
        "time_zone_offset" to timeZoneOffset,
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

private fun getCurrentTimeZoneOffset(): Int {
    val timeZone = TimeZone.getDefault()
    val rawOffset = timeZone.rawOffset
    return rawOffset/1000
}

private fun getCurrentTimeZoneName(): String {
    val timeZone = TimeZone.getDefault()
    return timeZone.getDisplayName(false, TimeZone.SHORT, Locale.getDefault())
}

private fun getCurrentTimeZone(): String {
    val timeZone = TimeZone.getDefault()
    return timeZone.id.toString()
}

private fun getCurrentLocale(): String {
    val locale = Locale.getDefault()
    return locale.toString()
}
