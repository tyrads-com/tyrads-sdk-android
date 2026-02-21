package com.tyrads.sdk

import AcmoConfig
import AcmoEndpointNames
import AcmoInitModel
import AcmoKeyNames
import AcmoTrackingController
import AcmoUsageStatsController
import TyradsActivity
import android.app.LocaleManager
import android.app.Activity
import android.app.Application
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
import kotlinx.coroutines.launch
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
import com.tyrads.sdk.acmo.modules.input_models.TyradsConfig
import com.tyrads.sdk.acmo.modules.webview.WebViewManager
import com.tyrads.sdk.acmo.modules.notifications.FCMService
import com.tyrads.sdk.acmo.modules.notifications.FCMNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import android.os.Bundle

interface TyradsCallback {
    fun onSuccess()
    fun onFailure(error: String)
}

interface TyradsLoginCallback {
    fun onSuccess(isNewUser: Boolean)
    fun onFailure(error: String)
}

@Keep
class Tyrads private constructor() {
    internal var apiKey: String? = null
    internal var apiSecret: String? = null
    internal var encKey: String? = null
    internal var engagementId: String? = null
    private var config: TyradsConfig = TyradsConfig()
    internal val tyradsConfig: TyradsConfig get() = config
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

    private var _isSecure: Boolean = false
    val isSecure: Boolean get() = _isSecure

    var premiumColor: String = "#1C90DF"
    var headerColor: String? = null
    var mainColor: String? = null
    private val _privacyAccepted = MutableStateFlow(false)
    val privacyAccepted: StateFlow<Boolean> = _privacyAccepted.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()


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

    private suspend inline fun safeCallback(crossinline block: () -> Unit) {
        withContext(Dispatchers.Main) {
            try {
                block()
            } catch (e: Exception) {
                log("Error in callback execution: ${e.message}", Log.ERROR)
            }
        }
    }

    suspend fun init(
        context: Context,
        apiKey: String,
        apiSecret: String,
        encryptionKey: String? = null,
        engagementId: String? = null,
        config: TyradsConfig = TyradsConfig(),
        debugMode: Boolean = false
    ) = withContext(Dispatchers.Default) {
        this@Tyrads.context = context.applicationContext
        this@Tyrads.apiKey = apiKey.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API key cannot be blank")
        this@Tyrads.encKey = encryptionKey
        this@Tyrads.engagementId = engagementId
        this@Tyrads.apiSecret = apiSecret.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API secret cannot be blank")
        setTyradsConfig(config)
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

        localizationService.init(currentLanguageCode.value)

        try {
            val integrityToken = getPlayIntegrityToken(context)
            log("Integrity Token: $integrityToken")
            preferences.edit { putString(AcmoKeyNames.PLAY_INTEGRITY_TOKEN, integrityToken) }
        } catch (error: Exception) {
            log("An Error Occurred: ${error.message}", Log.ERROR)
        }

        // Initialize FCM Service
        try {
            FCMService.initialize(context)
            registerLifecycleCallbacks(context)
        } catch (error: Exception) {
            log("Failed to initialize FCM: ${error.message}", Log.ERROR)
        }

        log("Tyrads SDK initialized", Log.INFO)
    }

    @JvmOverloads
    fun init(
        context: Context,
        apiKey: String,
        apiSecret: String,
        encryptionKey: String? = null,
        engagementId: String? = null,
        config: TyradsConfig = TyradsConfig(),
        debugMode: Boolean = false,
        callback: TyradsCallback
    ) {
        tyradScope.launch {
            try {
                init(context, apiKey, apiSecret, encryptionKey, engagementId, config, debugMode)
                safeCallback { callback.onSuccess() }
            } catch (e: Exception) {
                log("Exception during init: ${e.message}", Log.ERROR)
                safeCallback { callback.onFailure(e.message ?: "Unknown error during initialization") }
            }
        }
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
            val fcmToken = preferences.getString(AcmoKeyNames.FCM_TOKEN, null)
            val engagementId = this@Tyrads.engagementId
            log("Device Details: $deviceDetails")

            val fd = mutableMapOf(
                "publisherUserId" to userId,
                "platform" to "Android",
                "identifierType" to identifierType,
                "identifier" to advertisingId,
                "devicePushToken" to fcmToken,
                "engagementId" to if (engagementId.isNullOrBlank()) null else engagementId.toInt(),
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
            log("Initialization Data : $fd", force = true)
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
                    this@Tyrads.token = loginData.data.token
                    this@Tyrads.mainColor =
                        loginData.data.publisherApp.mainColor.ifBlank { "#1C90DF" }
                    this@Tyrads.premiumColor =
                        loginData.data.publisherApp.premiumColor.ifBlank { "#1C90DF" }
                    this@Tyrads.headerColor =
                        loginData.data.publisherApp.headerColor.ifBlank { "#000000" }
                    initializePrivacyStatus()

                    try {
                        this@Tyrads.newUser = NetworkCommons().isNewUser()
                    } catch (e: Exception) {
                        this@Tyrads.newUser = loginData.data.newRegisteredUser
                    }

                    if (privacyAccepted.value) {
                        val usageStatsController = AcmoUsageStatsController()
                        usageStatsController.saveUsageStats()
                    }

                    _isLoggedIn.value = true
                    track(TyradsActivity.INITIALIZED)

                    // ✅ Preload WebView after successful login
                    log("Calling _preloadWebView() after successful login", Log.INFO, force = true)
                    _preloadWebView()

                    return@withContext true
                }

                is Result.Failure -> {
                    log("User login failed", Log.ERROR, force = true)
                    val error = result.getException()
                    val errorMessage = String(response.data)
                    log("Error: ${error.message}", force = true)
                    log("Server Message: $errorMessage", force = true)
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            log("Exception during login: ${e.message}", Log.ERROR)
            return@withContext false
        }
    }

    @JvmOverloads
    fun loginUser(userID: String? = null, callback: TyradsLoginCallback) {
        tyradScope.launch {
            try {
                val success = loginUser(userID)
                safeCallback {
                    if (success) {
                        callback.onSuccess(newUser)
                    } else {
                        callback.onFailure("Login failed - Please check your credentials and try again")
                    }
                }
            } catch (e: Exception) {
                log("Exception during loginUser: ${e.message}", Log.ERROR)
                safeCallback { callback.onFailure(e.message ?: "Unknown error during login") }
            }
        }
    }

    internal fun getWebUri(route: String? = null, campaignID: Int? = null): String {
        val skipUserInfo = getSkipUserInfo()
        val currentRoute = route ?: ""
        Log.w("bmd", "getWebUri: $currentRoute")

        return Uri.Builder()
            .scheme("https")
            .authority("sdk.tyrads.com")
            .appendQueryParameter(
                "to",
                when {
                    campaignID == null -> currentRoute
                    else -> "$currentRoute/$campaignID"
                }
            )
            .appendQueryParameter("token", token)
            .appendQueryParameter("lang", currentLanguageCode.value)
            .appendQueryParameter("skipUserInfo", skipUserInfo.toString())
            .build()
            .toString()
    }

    private fun _preloadWebView() {
        try {
            log("_preloadWebView: Starting preload", Log.INFO, force = true)

            url = getWebUri()
            log("_preloadWebView: Built URL: $url", Log.INFO, force = true)

            WebViewManager.getInstance().preload(context, url)

            log("_preloadWebView: Preload initiated successfully", Log.INFO, force = true)
        } catch (e: Exception) {
            log("_preloadWebView: Error: ${e.message}", Log.ERROR, force = true)
        }
    }

    fun preloadAfterClose() {
        tyradScope.launch {
            try {
                log("preloadAfterClose: Re-preloading WebView after close", Log.INFO, force = true)
                _preloadWebView()
            } catch (e: Exception) {
                log("preloadAfterClose: Error: ${e.message}", Log.ERROR, force = true)
            }
        }
    }

    suspend fun showOffers(route: String? = null, campaignID: Int? = null) =
        withContext(Dispatchers.Default) {
            log("showOffers: Preparing to show offers", Log.INFO, force = true)

            if (!::loginData.isInitialized) {
                log("showOffers: User initialization error", Log.ERROR)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please try back later", Toast.LENGTH_LONG).show()
                }
                return@withContext
            }

            val requestedUrl = getWebUri(route, campaignID)
            val hasPreloadedWebView = WebViewManager.getInstance().getHeadlessWebView() != null

            if (url != requestedUrl || !hasPreloadedWebView) {
                url = requestedUrl
                log(
                    "showOffers: URL changed or no preload available - preloading now: $url",
                    Log.INFO,
                    force = true
                )

                WebViewManager.getInstance().preload(context, url)
            } else {
                log("showOffers: Using existing preloaded WebView", Log.INFO, force = true)
            }

            log("showOffers: Launching AcmoApp activity", Log.INFO, force = true)
            val intent = Intent(context, AcmoApp::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            track(TyradsActivity.opened)
        }

    @JvmOverloads
    fun showOffers(
        route: String? = null,
        campaignID: Int? = null,
        callback: TyradsCallback? = null
    ) {
        tyradScope.launch {
            try {
                showOffers(route, campaignID)
                callback?.let { safeCallback { it.onSuccess() } }
            } catch (e: Exception) {
                log("Exception during showOffers: ${e.message}", Log.ERROR)
                callback?.let {
                    safeCallback {
                        it.onFailure(
                            e.message ?: "Unknown error showing offers"
                        )
                    }
                }
            }
        }
    }

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

    @JvmOverloads
    fun changeLanguage(languageCode: String, callback: TyradsCallback? = null) {
        tyradScope.launch {
            try {
                changeLanguage(languageCode)
                callback?.let { safeCallback { it.onSuccess() } }
            } catch (e: Exception) {
                log("Exception during changeLanguage: ${e.message}", Log.ERROR)
                callback?.let {
                    safeCallback {
                        it.onFailure(
                            e.message ?: "Unknown error changing language"
                        )
                    }
                }
            }
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

    private fun setTyradsConfig(config: TyradsConfig) {
        this.config = config
        Log.d("Tyrads", "Config updated: $config")
    }

    fun setUserInfo(userInfo: TyradsUserInfo) {
        this.userInfo = userInfo
    }

    private fun getSkipUserInfo(): Boolean {
        val key = "${AcmoKeyNames.SKIP_USER_INFO}${publisherUserID}"
        return preferences.getBoolean(key, false)
    }

    private fun registerLifecycleCallbacks(context: Context) {
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                FCMNotifications.getInstance().handleNotificationIntent(activity.intent)
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                FCMNotifications.getInstance().handleNotificationIntent(activity.intent)
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })

        if (context is Activity) {
            FCMNotifications.getInstance().handleNotificationIntent(context.intent)
        }
    }
}