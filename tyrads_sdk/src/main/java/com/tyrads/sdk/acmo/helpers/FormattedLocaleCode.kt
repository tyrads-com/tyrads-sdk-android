package com.tyrads.sdk.acmo.helpers

import android.util.Log
import java.util.Locale

fun getFormattedLocaleCode(): String {
    val locale = Locale.getDefault()
    val tag = locale.toLanguageTag()

    Log.d("TyradsSDK", "getFormattedLocaleCode: $tag")
    Log.d("TyradsSDK", "language: ${locale.language}")
    if (locale.language != "zh"){
        return locale.language
    }
    return tag
}