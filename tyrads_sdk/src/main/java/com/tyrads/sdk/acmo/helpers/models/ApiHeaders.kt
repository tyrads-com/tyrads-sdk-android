package com.tyrads.sdk.acmo.helpers.models

data class ApiHeaders(
    val xApiKey: String,
    val xApiSecret: String,
    val xUserId: String,
    val xSdkPlatform: String?,
    val xSdkVersion: String?,
    val userAgent: String,
    val languageCode: String,
    val premiumColor: String,
    val headerColor: String,
    val mainColor: String,
)