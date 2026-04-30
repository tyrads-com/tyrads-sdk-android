package com.tyrads.sdk

import AcmoConfig
import AcmoEndpointNames
import AcmoKeyNames
import AcmoTrackingController
import AcmoUsageStatsController
import TyradsActivity
import android.app.Activity
import android.app.Application
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.tyrads_sdk_gitlab.acmo.modules.device_details.AcmoDeviceDetailsController
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import com.tyrads.sdk.acmo.core.AcmoApp
import androidx.appcompat.app.AppCompatDelegate
import com.tyrads.sdk.acmo.helpers.isGooglePlayServicesAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.util.UUID
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.edit
import com.tyrads.sdk.acmo.core.AcmoOnboardingGate
import com.tyrads.sdk.acmo.core.services.LocalizationService
import com.tyrads.sdk.acmo.core.utils.getPlayIntegrityToken
import com.tyrads.sdk.acmo.helpers.AcmoEncrypt
import com.tyrads.sdk.acmo.helpers.models.ApiHeaders
import com.tyrads.sdk.acmo.modules.input_models.AcmoInitModel
import com.tyrads.sdk.acmo.modules.input_models.TyradsConfig
import com.tyrads.sdk.acmo.modules.premium_widgets.TopOffers
import com.tyrads.sdk.acmo.modules.push_notifications.FCMNotifications
import com.tyrads.sdk.acmo.modules.push_notifications.FCMService
import com.tyrads.sdk.acmo.modules.webview.WebViewManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Keep
class Tyrads private constructor() {
    internal var apiKey: String? = null
    internal var apiSecret: String? = null
    internal var encKey: String? = null
    internal var engagementId: String? = null
    internal var placementId: String? = null
    internal var token: String? = null
    var config: TyradsConfig = TyradsConfig()
    internal var publisherUserID: String? = null
    internal lateinit var context: Context
    internal lateinit var preferences: SharedPreferences
    internal lateinit var loginData: AcmoInitModel
    internal var newUser: Boolean = false
    var initWait: Job? = null
    var loginUserWait: Job? = null
    lateinit var navController: NavHostController
    internal var debugMode: Boolean = false

    val tyradScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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
        encKey: String? = null,
        engagementId: String? = null,
        placementId: String? = null,
        config: TyradsConfig = TyradsConfig(),
        debugMode: Boolean = false
    ) = withContext(Dispatchers.Default) {
        this@Tyrads.context = context.applicationContext
        this@Tyrads.apiKey = apiKey.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API key cannot be blank")
        this@Tyrads.apiSecret = apiSecret.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API secret cannot be blank")
        this@Tyrads.encKey = encKey
        this@Tyrads.engagementId = engagementId
        this@Tyrads.placementId = placementId
        setTyradsConfig(config)
        this@Tyrads.debugMode = debugMode

        Log.i("Tyrads", "apiKey: $apiKey \n apiSecret: $apiSecret")
        preferences = context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE)
        preferences.edit {
            putString(AcmoKeyNames.API_KEY, apiKey)
            putString(AcmoKeyNames.API_SECRET, apiSecret)
        }
        log(
            "Warning: debugMode is set to true. This should not be used in production.",
            Log.WARN
        )
        log("Tyrads SDK initialized", Log.INFO)
        if (!encKey.isNullOrBlank()) {
            _isSecure = true
        }
        try {
            NetworkCommons()
            var currentLanguage = preferences.getString(AcmoKeyNames.LANGUAGE, null)

            if (currentLanguage.isNullOrBlank()) {
                currentLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.getSystemService(LocaleManager::class.java).applicationLocales[0]?.toLanguageTag()
                        ?.split("-")?.first() ?: "en"
                } else {
                    AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()?.split("-")
                        ?.first() ?: "en"
                }
            }
            _currentLanguageCode.value = currentLanguage
            log("Selected Language: ${currentLanguageCode.value}")

            localizationService.init(currentLanguageCode.value)
        } catch (e: Exception) {
            log("Error during initialization setup: ${e.message}", Log.ERROR)
        }

        log("Tyrads SDK initialized", Log.INFO)
        try {
            initializePrivacyStatus()
        } catch (e: Exception) {
            log("Error initializing privacy status: ${e.message}", Log.ERROR)
        }
        try {
            val integrityToken = getPlayIntegrityToken(context)
            log("Integrity Token: $integrityToken")
            preferences.edit { putString(AcmoKeyNames.PLAY_INTEGRITY_TOKEN, integrityToken) }
            FCMService.initialize(context)
            registerLifecycleCallbacks(context)
        } catch (e: Exception) {
            log("$e", Log.ERROR)
        }
    }

    suspend fun loginUser(userID: String? = null): ApiHeaders? = withContext(Dispatchers.Default) {
        try {
            // Wait for initialization to complete
            if (initWait?.isCompleted == false) {
                log("Waiting for init setup to complete", Log.DEBUG, true)
            }
            initWait?.join()
            if (!::preferences.isInitialized) {
                log("loginUser: init setup error", Log.ERROR)
                return@withContext null
            }
            log("Starting user login process", Log.INFO)
            val userId = userID ?: preferences.getString(AcmoKeyNames.USER_ID, "") ?: ""
            var advertisingId: String? = ""
            val fcmToken = preferences.getString(AcmoKeyNames.FCM_TOKEN, null)
            var identifierType = ""
            try {
                if (isGooglePlayServicesAvailable(context)) {
                    advertisingId =
                        AdvertisingIdClient.getAdvertisingIdInfo(context).id.toString()
                    identifierType = "GAID"
                }
            } catch (e: Exception) {
                log("Error getting advertising id", Log.ERROR)
            }

            if (advertisingId.isNullOrBlank()) {
                advertisingId = preferences.getString("uuid", null) ?: run {
                    val newUuid = UUID.randomUUID().toString()
                    preferences.edit().putString("uuid", newUuid).apply()
                    newUuid
                }
                identifierType = "OTHER"
            }
            val engagementId = this@Tyrads.engagementId
            val deviceDetailsController = AcmoDeviceDetailsController()
            val deviceDetails = deviceDetailsController.getDeviceDetails()
            Log.i("DeviceDetails:", deviceDetails.toString())

            val fd = mutableMapOf(
                "publisherUserId" to userId,
                "platform" to "Android",
                "devicePushToken" to fcmToken,
                "identifierType" to identifierType,
                "identifier" to advertisingId,
                "engagementId" to if (engagementId.isNullOrBlank()) null else engagementId.toInt(),
                "deviceData" to deviceDetails
            )
            log("Initialization Data: $fd")
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
                info.age?.let { fd["age"] = it }
                info.gender?.let { fd["gender"] = it }
            }

            val body =
                if (_isSecure) AcmoEncrypt(encryptionKey = encKey!!).encryptDataAESGCM(
                    data = fd
                ) else fd

            val (request, response, result) = Fuel.post(AcmoEndpointNames.INITIALIZE)
                .body(Gson().toJson(body))
                .response()

            when (result) {
                is Result.Success -> {
                    log("User login successful ${response.data}", Log.INFO)
                    val jsonString = String(response.data)
                    Log.e("Data", jsonString)
                    loginData = Gson().fromJson(jsonString, AcmoInitModel::class.java)
                    publisherUserID = loginData.data.accountInfo.publisherUserId
                    preferences.edit { putString(AcmoKeyNames.USER_ID, publisherUserID) }
                    this@Tyrads.token = loginData.data.token

                    mainColor = loginData.data.appInfo.mainColor.ifBlank { "#1C90DF" }
                    premiumColor =
                        loginData.data.appInfo.premiumColor.ifBlank { "#1C90DF" }
                    headerColor = loginData.data.appInfo.headerColor.ifBlank { "#000000" }

                    if (preferences.getBoolean(
                            AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + publisherUserID,
                            false
                        )
                    ) {
                        val usageStatsController = AcmoUsageStatsController()
                        usageStatsController.saveUsageStats()
                    }
                    try {
                        newUser = NetworkCommons().isNewUser()
                    } catch (e: Exception) {
                        newUser = loginData.data.newRegisteredUser
                    }

                    track(TyradsActivity.INITIALIZED)
                    _isLoggedIn.value = true

                    // Preload WebView after successful login (from webview-preload branch)
                    log("Calling _preloadWebView() after successful login", Log.INFO, force = true)
                    _preloadWebView()

                    val apiKey = preferences.getString(AcmoKeyNames.API_KEY, null) ?: ""
                    val apiSecret = preferences.getString(AcmoKeyNames.API_SECRET, null) ?: ""
                    val sdkPlatform = AcmoConfig.SDK_PLATFORM
                    val sdkVersion = AcmoConfig.SDK_VERSION
                    val userAgent = "Android"

                    return@withContext ApiHeaders(
                        xApiKey = apiKey,
                        xApiSecret = apiSecret,
                        xUserId = userId,
                        xSdkPlatform = sdkPlatform,
                        xSdkVersion = sdkVersion,
                        userAgent = userAgent,
                        languageCode = currentLanguageCode.value,
                        premiumColor = loginData.data.appInfo.premiumColor,
                        headerColor = loginData.data.appInfo.headerColor,
                        mainColor = loginData.data.appInfo.mainColor,
                    )
                }

                is Result.Failure -> {
                    log("User login failed", Log.ERROR, force = true)
                    val error = result.getException()
                    val errorMessage = String(response.data)
                    log("Error: ${error.message}")
                    log("Server Message: $errorMessage")
                    return@withContext null
                }
            }

        } catch (e: Exception) {
            log("Exception during login: ${e.message}", Log.ERROR)
            return@withContext null
        }
    }

    internal fun getWebUri(route: String? = null, campaignID: Int? = null): String {
        val skipUserInfo = getSkipUserInfo()
        val currentRoute = route ?: ""

        val builder = Uri.Builder()
            .scheme("https")
            .authority("v4.sdk.tyrads.com")
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

        if (!placementId.isNullOrBlank()) {
            builder.appendQueryParameter("placementId", placementId)
        }

        return builder.build().toString()
    }

    private fun _preloadWebView() {
        try {
            log("_preloadWebView: Starting preload", Log.INFO, force = true)

            // Build URL
            url = getWebUri()
            log("_preloadWebView: Built URL: $url", Log.INFO, force = true)

            // Preload the WebView
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
            log("Preparing to show offers", Log.INFO)
            loginUserWait?.join()
            if (!::loginData.isInitialized) {
                log("showOffers: User initialization error", Log.ERROR)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please try back later", Toast.LENGTH_LONG).show()
                }
                return@withContext
            }
            // Build the requested URL
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

    fun isPrivacyAccepted(): Boolean {
        initializePrivacyStatus()
        return privacyAccepted.value
    }

    suspend fun checkOnboardingProcess(context: Context): Boolean =
        withContext(Dispatchers.Default) {
            try {
                val result = AcmoOnboardingGate.start(context)
                return@withContext result
            } catch (e: Exception) {
                log("Onboarding check failed: ${e.message}", Log.ERROR)
                return@withContext false
            }
        }

    enum class PremiumWidgetStyles { LIST, SLIDER_CARDS }

    @Composable
    fun TopPremiumOffers(
        showMore: Boolean = true,
        showMyOffers: Boolean = true,
        showMyOffersEmptyView: Boolean = false,
        widgetStyle: PremiumWidgetStyles = PremiumWidgetStyles.LIST,
    ) {
        TopOffers(
            widgetStyle = widgetStyle,
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

    fun setUpSDKVersion(sdkVersion: String) {
        AcmoConfig.SDK_VERSION = sdkVersion
    }

    fun setUserInfo(userInfo: TyradsUserInfo) {
        this.userInfo = userInfo
    }

    private fun getSkipUserInfo(): Boolean {
        val key = "${AcmoKeyNames.SKIP_USER_INFO}${publisherUserID}"
        return preferences.getBoolean(key, false)
    }

    private fun setTyradsConfig(config: TyradsConfig) {
        this.config = config
    }

    // Notification lifecycle callbacks (from notification branch)
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