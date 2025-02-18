package com.tyrads.sdk.acmo.core.localization.helper

import AcmoKeyNames
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tyrads.sdk.Tyrads
import java.util.Locale

object LocalizationHelper {

    fun changeLanguagee(context: Context, languageCode: String) {
        try {
            Log.i("Localization", "Attempting to change language to: $languageCode")

            // Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
                Log.i("Localization", "Locale set via LocaleManager: ${localeManager.applicationLocales}")
            } else {
                // Android < 13
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
                Log.i("Localization", "Locale set via AppCompatDelegate: ${AppCompatDelegate.getApplicationLocales()}")
            }
            Tyrads.getInstance().preferences.edit().putString(AcmoKeyNames.LANGUAGE, languageCode).apply()
            Log.i("Localization", "Saved language to preferences: $languageCode")

        } catch (e: Exception) {
            Log.e("Localization", "Error while changing language: ${e.localizedMessage}")
        }
    }

    fun changeLanguage(context: Context, languageCode: String) {
        try {
            Log.i("Localization", "Attempting to change language to: $languageCode")

            // Create a new Locale object
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            // Create a new Configuration object
            val config = context.resources.configuration
            config.setLocale(locale)

            // Update the resources with the new configuration
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            // For Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList.forLanguageTags(languageCode)
                Log.i("Localization", "Locale set via LocaleManager: ${localeManager?.applicationLocales}")
            } else {
                // For Android < 13
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
                Log.i("Localization", "Locale set via AppCompatDelegate: ${AppCompatDelegate.getApplicationLocales()}")
            }

            Tyrads.getInstance().preferences.edit().putString(AcmoKeyNames.LANGUAGE, languageCode).apply()
            Log.i("Localization", "Saved language to preferences: $languageCode")

        } catch (e: Exception) {
            Log.e("Localization", "Error while changing language: ${e.localizedMessage}")
        }
    }



    fun getLanguageCode(context: Context): String {
        var currentLanguage = Tyrads.getInstance().preferences.getString(AcmoKeyNames.LANGUAGE, null)

        if(currentLanguage.isNullOrBlank()) {
            currentLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java).applicationLocales[0]?.toLanguageTag()?.split("-")?.first() ?: "en"
            } else {
                AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()?.split("-")?.first() ?: "en"
            }
        }

        return currentLanguage
    }

    fun setDeviceDefaultLanguage(context: Context){
        Tyrads.getInstance().preferences.edit().remove(AcmoKeyNames.LANGUAGE).apply()
    }
}
