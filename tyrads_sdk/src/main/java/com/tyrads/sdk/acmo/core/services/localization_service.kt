package com.tyrads.sdk.acmo.core.services

import AcmoConfig
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit

@Keep
data class TranslationVersionResponse(
    val data: List<LocaleInfo>
)

@Keep
data class LocaleInfo(
    val code: String,
    val sha256: String
)

class LocalizationService private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: LocalizationService? = null

        fun getInstance(): LocalizationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalizationService().also { INSTANCE = it }
            }
        }
    }

    private val gson = Gson()
    private var translations: Map<String, Any> = emptyMap()
    private var supportedLocales: MutableList<String> = mutableListOf()
    private val prefs: SharedPreferences?
        get() = Tyrads.getInstance().safePreferences
    private val fallbackLocale = "en"

    // Deserializers for Fuel HTTP responses
    private object TranslationDeserializer : ResponseDeserializable<Map<String, Any>> {
        override fun deserialize(content: String): Map<String, Any> {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            return Gson().fromJson(content, type)
        }
    }

    private object VersionDeserializer : ResponseDeserializable<TranslationVersionResponse> {
        override fun deserialize(content: String): TranslationVersionResponse {
            return Gson().fromJson(content, TranslationVersionResponse::class.java)
        }
    }

    suspend fun init(locale: String) {
        loadTranslations(locale)
    }

    private suspend fun loadTranslations(locale: String, force: Boolean = false) {
        val hasUpdate = checkForUpdate(locale, force)

        // Match Dart behavior: only check cache if no update is available
        if (!hasUpdate) {
            val cachedData = prefs?.getString("translations_$locale", null)
            if (cachedData != null) {
                try {
                    val type = object : TypeToken<Map<String, Any>>() {}.type
                    translations = gson.fromJson(cachedData, type)
                    return
                } catch (e: Exception) {
                    Log.e("LocalizationService", "Error parsing cached translations: ${e.message}")
                    // Continue to fetch fresh data if cached data is corrupted
                }
            }
        }

        fetchTranslations(locale, force)
    }

    private suspend fun fetchTranslations(locale: String, force: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val actualLocale = if (!supportedLocales.contains(locale)) {
                    fallbackLocale
                } else {
                    locale
                }

                val url = "${AcmoConfig.BASE_URL}translations/$actualLocale"

                val result = Fuel.get(url, listOf(
                    "force" to force.toString(),
                    "format" to "nested"
                )).awaitObject(TranslationDeserializer)

                translations = result

                // Cache the translations
                val jsonString = gson.toJson(result)
                prefs?.edit {
                    putString("translations_$actualLocale", jsonString)
                }

            } catch (error: FuelError) {
                Log.e("LocalizationService", "Failed to load translations: ${error.response.statusCode}")
            } catch (e: Exception) {
                Log.e("LocalizationService", "Network error fetching translations: ${e.message}")
            }
        }
    }

    private suspend fun checkForUpdate(locale: String, force: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "${AcmoConfig.BASE_URL}translations/version"

                val response = Fuel.get(url, listOf(
                    "force" to force.toString()
                )).awaitObject(VersionDeserializer)

                supportedLocales = response.data.map { it.code }.toMutableList()

                if (!supportedLocales.contains(locale)) {
                    return@withContext false
                }

                val currentLocaleInfo = response.data.firstOrNull { it.code == locale }
                if (currentLocaleInfo == null) {
                    return@withContext false
                }

                val currentLocaleSha256 = currentLocaleInfo.sha256
                val cachedVersion = prefs?.getString("cached_version_$locale", null)

                if (currentLocaleSha256 != cachedVersion) {
                    // Match Dart behavior: remove then set
                    prefs?.edit {
                        remove("cached_version_$locale")
                        putString("cached_version_$locale", currentLocaleSha256)
                    }
                    return@withContext true
                }

                false
            } catch (e: Exception) {
                Log.e("LocalizationService", "Error checking for update: ${e.message}")
                false
            }
        }
    }

    // Fixed: Return String instead of String? to match Dart behavior
    fun translate(key: String, args: Map<String, Any>? = null): String {
        if (translations.isEmpty()) {
            return key
        }

        val keys = key.split(".")
        var currentMap: Any? = translations

        for (k in keys) {
            when (currentMap) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val map = currentMap as Map<String, Any>
                    if (map.containsKey(k)) {
                        currentMap = map[k]
                    } else {
                        return key // Return key if path not found
                    }
                }
                else -> return key // Return key if not a map
            }
        }

        // Check if final result is a string
        if (currentMap is String) {
            var translated: String = currentMap
            args?.forEach { (argKey, argValue) ->
                translated = translated.replace(
                    Regex("\\{$argKey\\}", RegexOption.IGNORE_CASE),
                    argValue.toString()
                )
            }
            return translated
        } else {
            return key // Return key if final result is not a string
        }
    }

    suspend fun changeLanguage(locale: String, force: Boolean = false) {
        loadTranslations(locale, force)
    }

    fun getSupportedLocales(): List<String> = supportedLocales.toList()
}