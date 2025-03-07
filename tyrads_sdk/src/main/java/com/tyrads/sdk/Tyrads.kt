package com.tyrads.sdk


import AcmoConfig
import AcmoEndpointNames
import AcmoInitModel
import AcmoKeyNames
import AcmoTrackingController
import AcmoUsageStatsController
import TyradsActivity
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
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper
import com.tyrads.sdk.acmo.helpers.isGooglePlayServicesAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import android.net.Uri
import android.widget.Toast
import com.tyrads.sdk.acmo.helpers.models.ApiHeaders
import com.tyrads.sdk.acmo.modules.dashboard.TopOffers
import kotlinx.coroutines.coroutineScope

@Keep
class Tyrads private constructor() {
    internal var apiKey: String? = null
    internal var apiSecret: String? = null
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
    internal var url: String? = null
    private var mediaSourceInfo: TyradsMediaSourceInfo? = null
    private var userInfo: TyradsUserInfo? = null
    private lateinit var currentLanguageCode: String

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

    fun init(context: Context, apiKey: String, apiSecret: String, debugMode: Boolean = false) {
        this.context = context.applicationContext
        this.apiKey = apiKey.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API key cannot be blank")
        this.apiSecret = apiSecret.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("API secret cannot be blank")
        this.debugMode = debugMode
        Log.i("bmd", "apiKey: $apiKey \n apiSecret: $apiSecret")
        initWait = tyradScope.launch {
            preferences = context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE)
            preferences.edit().putString(AcmoKeyNames.API_KEY, apiKey).apply()
            preferences.edit().putString(AcmoKeyNames.API_SECRET, apiSecret).apply()
            NetworkCommons()
            currentLanguageCode  = LocalizationHelper.getLanguageCode(context)

            log(
                "Warning: debugMode is set to true. This should not be used in production.",
                Log.WARN
            )
            log("Tyrads SDK initialized", Log.INFO)
        }
    }

    suspend fun loginUser(userID: String? = null): ApiHeaders? {
        return try {
            coroutineScope {
                // Wait for initialization to complete
                if (initWait?.isCompleted == false) {
                    log("Waiting for init setup to complete", Log.DEBUG, true)
                }
                initWait?.join()
                if (!::preferences.isInitialized) {
                    log("loginUser: init setup error", Log.ERROR)
                    return@coroutineScope null
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
                        preferences.edit().putString("uuid", newUuid).apply()
                        newUuid
                    }
                    identifierType = "OTHER"
                }

                val deviceDetailsController = AcmoDeviceDetailsController()
                val deviceDetails = deviceDetailsController.getDeviceDetails()

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

                val (request, response, result) = Fuel.post(AcmoEndpointNames.INITIALIZE)
                    .body(Gson().toJson(fd))
                    .response()

                when (result) {
                    is Result.Success -> {
                        log("User login successful", Log.INFO)
                        val jsonString = String(response.data)
                        loginData = Gson().fromJson(jsonString, AcmoInitModel::class.java)
                        publisherUserID = loginData.data.user.publisherUserId
                        preferences.edit().putString(AcmoKeyNames.USER_ID, publisherUserID).apply()
                        newUser = loginData.data.newRegisteredUser

                        if (preferences.getBoolean(
                                AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + publisherUserID,
                                false
                            )
                        ) {
                            val usageStatsController = AcmoUsageStatsController()
                            usageStatsController.saveUsageStats()
                        }

                        track(TyradsActivity.initialized)

                        val apiKey = preferences.getString(AcmoKeyNames.API_KEY, null) ?: ""
                        val apiSecret = preferences.getString(AcmoKeyNames.API_SECRET, null) ?: ""
                        val sdkPlatform = AcmoConfig.SDK_PLATFORM
                        val sdkVersion = AcmoConfig.SDK_VERSION
                        val userAgent = "Android"

                        ApiHeaders(
                            xApiKey = apiKey,
                            xApiSecret = apiSecret,
                            xUserId = userId,
                            xSdkPlatform = sdkPlatform,
                            xSdkVersion = sdkVersion,
                            userAgent = userAgent,
                            languageCode = currentLanguageCode,
                            premiumColor = loginData.data.publisherApp.premiumColor,
                            headerColor = loginData.data.publisherApp.headerColor,
                            mainColor = loginData.data.publisherApp.mainColor,
                        )
                    }

                    is Result.Failure -> {
                        log("User login failed", Log.ERROR, force = true)
                        val error = result.getException()
                        val errorMessage = String(response.data)
                        log("Error: ${error.message}")
                        log("Server Message: $errorMessage")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            log("Exception during login: ${e.message}", Log.ERROR)
            null
        }
    }

    fun showOffers(route: String? = null, campaignID: Int? = null) {
        tyradScope.launch {
            log("Preparing to show offers", Log.INFO)
            if (loginUserWait?.isCompleted == false) {
                log("Waiting for user initialization to complete", Log.DEBUG, true)
            }
            loginUserWait?.join()
            if (!::loginData.isInitialized) {
                log("showOffers: User initialization error", Log.ERROR)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Please try back later", Toast.LENGTH_LONG).show()
                }
                return@launch
            }
            log("Launching offers", Log.INFO)
            url = Uri.Builder()
                .scheme("https")
                .authority("websdk.tyrads.com")
                .appendQueryParameter("apiKey", apiKey)
                .appendQueryParameter("apiSecret", apiSecret)
                .appendQueryParameter("userID", publisherUserID)
                .appendQueryParameter("newUser", newUser.toString())
                .appendQueryParameter("platform", "Android")
                .appendQueryParameter("hc", loginData.data.publisherApp.headerColor)
                .appendQueryParameter("mc", loginData.data.publisherApp.mainColor)
                .appendQueryParameter("pc", loginData.data.publisherApp.premiumColor)
                .appendQueryParameter("route", route?.toString())
                .appendQueryParameter("campaignID", campaignID?.toString())
                .appendQueryParameter("sdk_version", AcmoConfig.SDK_VERSION)
                .appendQueryParameter("av", AcmoConfig.AV)
                .appendQueryParameter("lang", currentLanguageCode)
                .build()
                .toString()
            Log.i("url", url.toString())
            val intent = Intent(context, AcmoApp::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun changeLanguage(context: Context, languageCode: String){
        tyradScope.launch {
            LocalizationHelper.changeLanguage(
                context, languageCode
            )
        }
    }
//    enum class TopOfferStyles {ONE, TWO, THREE, FOUR}
    @Composable
    fun TopPremiumOffers(
        showMore: Boolean = true,
        showMyOffers: Boolean = true,
        showMyOffersEmptyView: Boolean = false,
        style: Int = 2,
    ){
        TopOffers(
            showMore = showMore,
            showMyOffers = showMyOffers,
            showMyOffersEmptyView = showMyOffersEmptyView,
            style = style,
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
