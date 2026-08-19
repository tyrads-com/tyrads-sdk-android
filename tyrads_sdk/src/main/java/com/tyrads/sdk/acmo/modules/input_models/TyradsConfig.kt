package com.tyrads.sdk.acmo.modules.input_models

data class TyradsConfig(
    val skipInitialPages: Boolean = false,
    val skipUserInfo: Boolean = false,
    val defaultAge: Int? = null,
    val enableSkipOnboarding: Boolean = false,
)