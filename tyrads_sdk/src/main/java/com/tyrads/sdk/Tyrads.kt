package com.tyrads.sdk

import AcmoConfig
import AcmoEndpointNames
import AcmoInitModel
import AcmoKeyNames
import AcmoTrackingController
import AcmoUsageStatsController
import TyradsActivity
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.tyrads.sdk.acmo.modules.device_details.AcmoDeviceDetailsController
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import com.tyrads.sdk.acmo.core.AcmoApp
import com.tyrads.sdk.acmo.core.services.LocalizationService
import com.tyrads.sdk.acmo.helpers.isGooglePlayServicesAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.util.UUID
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.tyrads.sdk.acmo.core.utils.getPlayIntegrityToken
import com.tyrads.sdk.acmo.helpers.AcmoEncrypt
import com.tyrads.sdk.acmo.modules.premium_widgets.TopOffers
import androidx.core.content.edit
import com.tyrads.sdk.acmo.helpers.TyradsViewHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Keep
class Tyrads private constructor() {
    internal var apiKey: String? = null
    internal var apiSecret: String? = null
    internal var encKey: String? = null
    internal var token: String = ""
    internal var publisherUserID: String? = null
    internal lateinit var context: Context
    internal lateinit var preferences: SharedPreferences
    internal lateinit var loginData: AcmoInitModel
    internal var newUser: Boolean = false
    lateinit var navController: NavHostController
    internal var debugMode: Boolean = false

    internal val tyradScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var tracker = AcmoTrackingController()
    internal var url: String = ""
    private var mediaSourceInfo: TyradsMediaSourceInfo? = null
    private var userInfo: TyradsUserInfo? = null

    private val _currentLanguageCode = MutableStateFlow("en")
    val currentLanguageCode: StateFlow<String>
        get() = _currentLanguageCode
    private val localizationService = LocalizationService.getInstance()

    private var _isSecure: Boolean = false;
    val isSecure: Boolean get() = _isSecure;

    // need these variables outside
    var premiumColor: String = "#1C90DF"
    var headerColor: String? = null
    var mainColor: String? = null
    private val _privacyAccepted = MutableStateFlow(false)
    val privacyAccepted: StateFlow<Boolean> = _privacyAccepted.asStateFlow()

    internal fun initializePrivacyStatus() {
        _privacyAccepted.value = preferences.getBoolean(
            AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + publisherUserID,
            false
        )
    }

    internal fun setPrivacyAccepted(isAccepted: Boolean) {
        preferences.edit {
            putBoolean(
                AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + publisherUserID,
                isAccepted
            )
        }
        _privacyAccepted.value = isAccepted
    }

    companion object {
        @Volatile
        private var instance: Tyrads? = null

        @JvmStatic
        fun getInstance(): Tyrads {
            return instance ?: synchronized(this) {
                instance ?: Tyrads().also {
                    instance = it
                }
            }
        }

        @JvmStatic
        fun getTyradsView(): TyradsViewHelper {
            return TyradsViewHelper
        }
    }

    internal fun log(message: String, level: Int = Log.DEBUG, force: Boolean = false) {
        if (debugMode || force) {
            when (level) {
                Log.DEBUG -> Log.d(AcmoConfig.TAG, message)
                Log.INFO -> Log.i(AcmoConfig.TAG, message)
                Log.WARN -> Log.w(AcmoConfig.TAG, message)
                Log.ERROR -> Log.e(AcmoConfig.TAG, message)
                else -> Log.d(AcmoConfig.TAG, message)
            }
        }
    }

    suspend fun init(
        context: Context,
        apiKey: String,
        apiSecret: String,
        encryptionKey: String? = null,
        debugMode: Boolean = false
    ) = withContext(Dispatchers.Default) {
        this@Tyrads.context = context.applicationContext
        this@Tyrads.apiKey = apiKey.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API key cannot be blank")
        this@Tyrads.encKey = encryptionKey
        this@Tyrads.apiSecret = apiSecret.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API secret cannot be blank")
        this@Tyrads.debugMode = debugMode
        Log.i("bmd", "apiKey: $apiKey \n apiSecret: $apiSecret")
        preferences = context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE)
        preferences.edit {
            putString(AcmoKeyNames.API_KEY, apiKey)
            putString(AcmoKeyNames.API_SECRET, apiSecret)
        }
        if (!encryptionKey.isNullOrBlank()) {
            _isSecure = true
        }

        NetworkCommons()

        var currentLanguage = preferences.getString(AcmoKeyNames.LANGUAGE, null)

        if (currentLanguage.isNullOrBlank()) {
            currentLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java).applicationLocales[0]?.toLanguageTag()
                    ?.split("-")?.first() ?: "en"
            } else {
                AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()?.split("-")?.first()
                    ?: "en"
            }
        }
        _currentLanguageCode.value = currentLanguage
        log("Selected Language: ${currentLanguageCode.value}")

        // Initialize localization service - similar to Dart's LocalizationService().init(selectedLanguage)
        localizationService.init(currentLanguageCode.value)

        val integrityToken = getPlayIntegrityToken(context)
        log("Integrity Token: $integrityToken")
        preferences.edit { putString(AcmoKeyNames.PLAY_INTEGRITY_TOKEN, integrityToken) }
        log("Tyrads SDK initialized", Log.INFO)
    }

    suspend fun loginUser(userID: String? = null): Boolean = withContext(Dispatchers.Default) {
        try {
            if (!::preferences.isInitialized) {
                log("loginUser: init setup error", Log.ERROR)
                return@withContext false
            }
            log("Starting user login process", Log.INFO)
            val userId = userID ?: preferences.getString(AcmoKeyNames.USER_ID, "") ?: ""
            var advertisingId: String? = ""
            var identifierType = ""
            try {
                if (isGooglePlayServicesAvailable(context)) {
                    advertisingId = AdvertisingIdClient.getAdvertisingIdInfo(context).id.toString()
                    identifierType = "GAID"
                }
            } catch (e: Exception) {
                log("Error getting advertising id", Log.ERROR)
            }

            if (advertisingId.isNullOrBlank()) {
                advertisingId = preferences.getString("uuid", null) ?: run {
                    val newUuid = UUID.randomUUID().toString()
                    preferences.edit() { putString("uuid", newUuid) }
                    newUuid
                }
                identifierType = "OTHER"
            }

            val deviceDetailsController = AcmoDeviceDetailsController()
            val deviceDetails = deviceDetailsController.getDeviceDetails()
            log("Device Details: $deviceDetails")

            val fd = mutableMapOf(
                "publisherUserId" to userId,
                "platform" to "Android",
                "identifierType" to identifierType,
                "identifier" to advertisingId,
                "deviceData" to deviceDetails
            )
            mediaSourceInfo?.let { info ->
                info.sub1?.let { fd["sub1"] = it }
                info.sub2?.let { fd["sub2"] = it }
                info.sub3?.let { fd["sub3"] = it }
                info.sub4?.let { fd["sub4"] = it }
                info.sub5?.let { fd["sub5"] = it }
                info.mediaSourceName?.let { fd["mediaSourceName"] = it }
                info.mediaSourceId?.let { fd["mediaSourceId"] = it }
                info.mediaSubSourceId?.let { fd["mediaSubSourceId"] = it }
                info.incentivized?.let { fd["incentivized"] = it }
                info.mediaAdsetName?.let { fd["mediaAdsetName"] = it }
                info.mediaAdsetId?.let { fd["mediaAdsetId"] = it }
                info.mediaCreativeName?.let { fd["mediaCreativeName"] = it }
                info.mediaCreativeId?.let { fd["mediaCreativeId"] = it }
                info.mediaCampaignName?.let { fd["mediaCampaignName"] = it }
            }

            userInfo?.let { info ->
                info.email?.let { fd["email"] = it }
                info.phoneNumber?.let { fd["phoneNumber"] = it }
                info.userGroup?.let { fd["userGroup"] = it }
            }
            log("Initialization Data : $fd")
            val encData =
                if (_isSecure) AcmoEncrypt(encryptionKey = encKey!!).encryptDataAESGCM(data = fd) else emptyMap()
            val (request, response, result) = Fuel.post(AcmoEndpointNames.INITIALIZE)
                .body(Gson().toJson(if (isSecure) encData else fd)).response()

            when (result) {
                is Result.Success -> {
                    log("User login successful", Log.INFO)
                    val jsonString = String(response.data)
                    loginData = Gson().fromJson(jsonString, AcmoInitModel::class.java)
                    publisherUserID = loginData.data.user.publisherUserId
                    preferences.edit() { putString(AcmoKeyNames.USER_ID, publisherUserID) }
                    newUser = loginData.data.newRegisteredUser
                    token = loginData.data.token

                    // check for empty
                    mainColor = loginData.data.publisherApp.mainColor.ifBlank { "#1C90DF" }
                    premiumColor = loginData.data.publisherApp.premiumColor.ifBlank { "#1C90DF" }
                    headerColor = loginData.data.publisherApp.headerColor.ifBlank { "#000000" }
                    initializePrivacyStatus()

                    if (privacyAccepted.value) {
                        val usageStatsController = AcmoUsageStatsController()
                        usageStatsController.saveUsageStats()
                    }

                    track(TyradsActivity.INITIALIZED)
                    return@withContext true
                }

                is Result.Failure -> {
                    log("User login failed", Log.ERROR, force = true)
                    val error = result.getException()
                    val errorMessage = String(response.data)
                    log("Error: ${error.message}")
                    log("Server Message: $errorMessage")
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            log("Exception during login: ${e.message}", Log.ERROR)
            return@withContext false
        }
    }

    suspend fun showOffers(route: String? = null, campaignID: Int? = null) =
        withContext(Dispatchers.Default) {
            log("Preparing to show offers", Log.INFO)
            if (!::loginData.isInitialized) {
                log("showOffers: User initialization error", Log.ERROR)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please try back later", Toast.LENGTH_LONG).show()
                }
                return@withContext
            }
            log("Launching offers", Log.INFO)
            url = Uri
                .Builder()
                .scheme("https")
                .authority("sdk.tyrads.com")
                .path(route)
                .appendQueryParameter("token", token)
                .build()
                .toString()
            Log.i("url", url.toString())
            val intent = Intent(context, AcmoApp::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

    /**
     * Changes the language of the SDK - similar to Dart implementation
     *
     * This method is used to change the language of the SDK.
     *
     * The [languageCode] parameter is the language code of the language
     * to be used. For example, "en" for English, or "es" for Spanish.
     *
     * The method is asynchronous and returns when the language has been changed.
     *
     * The Tyrads SDK supports the following languages:
     * - English (en)
     * - Spanish (es)
     * - Indonesian (id)
     * - Japanese (ja)
     * - Korean (ko)
     * - Chinese (China, Simplified) (zh-Hans-CN)
     *
     * Note that the language change is persisted in the app's preferences,
     * so the next time the app is started, the SDK will use the new language.
     */
    suspend fun changeLanguage(languageCode: String) = withContext(Dispatchers.Default) {
        try {
            _currentLanguageCode.value = languageCode

            preferences.edit {
                putString(AcmoKeyNames.LANGUAGE, languageCode)
            }

            localizationService.changeLanguage(languageCode)

            log("Language changed to: $languageCode")
        } catch (e: Exception) {
            log("Error changing language: ${e.message}", Log.ERROR)
        }
    }

    enum class PremiumWidgetStyles {
        SLIDER_CARDS,
        LIST
    }

    @Composable
    fun TopPremiumOffers(
        widgetStyle: PremiumWidgetStyles = PremiumWidgetStyles.LIST,
    ) {
        TopOffers(
            widgetStyle = widgetStyle
        )
    }

    @Composable
    fun Dialog(content: @Composable () -> Unit) {
        content()
    }

    fun track(activity: String) {
        tracker.trackUser(activity)
    }

    fun setMediaSourceInfo(mediaSourceInfo: TyradsMediaSourceInfo) {
        this.mediaSourceInfo = mediaSourceInfo
    }

    fun setUserInfo(userInfo: TyradsUserInfo) {
        this.userInfo = userInfo
    }
}