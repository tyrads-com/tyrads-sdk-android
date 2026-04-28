package com.tyrads.sdk.acmo.core.localization.helper

import AcmoKeyNames
import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tyrads.sdk.Tyrads
import java.util.Locale

object LocalizationHelper {

    fun changeLanguage(context: Context, languageCode: String, shouldRecreate: Boolean = true) {
        try {
            val currentLanguage = getLanguageCode(context)

            Log.i("Localization", "Attempting to change language to: $languageCode")
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)

            context.createConfigurationContext(config)
            // Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
                Log.i(
                    "Localization",
                    "Locale set via LocaleManager: ${localeManager.applicationLocales}"
                )
            } else {
                // Android < 13
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(
                        languageCode
                    )
                )
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                Log.i(
                    "Localization",
                    "Locale set via AppCompatDelegate: ${AppCompatDelegate.getApplicationLocales()}"
                )
            }
            if (currentLanguage == languageCode || !shouldRecreate) {
                //Eat 5 star do nothing
            } else {
                (context as? Activity)?.recreate()
            }
            context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE).edit()
                .putString(AcmoKeyNames.LANGUAGE, languageCode).apply()
            Log.i("Localization", "Saved language to preferences: $languageCode")

        } catch (e: Exception) {
            Log.e("Localization", "Error while changing language: ${e.localizedMessage}")
        }
    }


    fun getLanguageCode(context: Context): String {
        try {
            Log.i("Localization", "in the get lang")
            val prefs = context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE)
            var currentLanguage = prefs.getString(AcmoKeyNames.LANGUAGE, null)

            if (currentLanguage.isNullOrBlank()) {
                currentLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.getSystemService(LocaleManager::class.java).applicationLocales[0]?.toLanguageTag()
                        ?.split("-")?.first() ?: "en"
                } else {
                    AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()?.split("-")
                        ?.first() ?: "en"
                }
            }

            return currentLanguage
        } catch (e: Exception) {
            Log.e(
                "Localization",
                "Error while getting saved language fallback to english: ${e.localizedMessage}"
            )
            return "en"
        }
    }

    fun setDeviceDefaultLanguage(context: Context) {
        try {
            context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE).edit()
                .remove(AcmoKeyNames.LANGUAGE).apply()
        } catch (e: Exception) {
            Log.e("Localization", "Error while removing saved language: ${e.localizedMessage}")
        }
    }

    fun applySavedLanguage(context: Context) {
        try {
            val savedLanguage = getLanguageCode(context)
            if (savedLanguage.isNotEmpty()) {
                changeLanguage(context, savedLanguage, shouldRecreate = false)
            }
        } catch (e: Exception) {
            Log.e("Localization", "Error while applying saved language: ${e.localizedMessage}")
        }
    }

    fun wrapContext(context: Context): Context {
        try {
            val languageCode = getLanguageCode(context)
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)

            return context.createConfigurationContext(config)
        } catch (e: Exception) {
            Log.e("Localization", "Error while wrapping context: ${e.localizedMessage}")
            return context
        }
    }
}
