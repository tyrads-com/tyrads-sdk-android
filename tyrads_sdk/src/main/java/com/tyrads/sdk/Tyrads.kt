package com.tyrads.sdk


import AcmoEndpointNames
import AcmoInitModel
import AcmoKeyNames
import AcmoUsageStatsController
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import com.example.tyrads_sdk_gitlab.acmo.modules.device_details.AcmoDeviceDetailsController
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class Tyrads private constructor() {
    internal var apiKey: String? = null
    internal var apiSecret: String? = null
    internal var publisherUserID: String? = null
    internal lateinit var context: Context
    internal lateinit var preferences: SharedPreferences
    internal lateinit var loginData: AcmoInitModel
    internal var newUser: Boolean = false
    var initializationWait: Job? = null


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

    fun init(context: Context, apiKey: String, apiSecret: String) {
        this.context = context
        this.apiKey = apiKey
        this.apiSecret = apiSecret
        preferences = context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE)
        preferences.edit().putString(AcmoKeyNames.API_KEY, apiKey).apply()
        preferences.edit().putString(AcmoKeyNames.API_SECRET, apiSecret).apply()
        NetworkCommons()
    }

     fun loginUser(userID: String? = null) {
         try {
              initializationWait = GlobalScope.launch {
                 val userId = userID ?: preferences.getString(AcmoKeyNames.USER_ID, "") ?: ""


                 val advertisingId = AdvertisingIdClient.getAdvertisingIdInfo(context).id.toString()
                 val identifierType = "GAID"
                 val deviceDetailsController = AcmoDeviceDetailsController()
                 val deviceDetails = deviceDetailsController.getDeviceDetails()

                 val fd = mapOf(
                     "publisherUserId" to userId,
                     "platform" to "Android",
                     "identifierType" to identifierType,
                     "identifier" to advertisingId,
                     "deviceData" to deviceDetails
                 )


                 val (request, response, result) = Fuel.post(AcmoEndpointNames.INITIALIZE)
                     .body(Gson().toJson(fd))
                     .response()

                 when (result) {
                     is Result.Success -> {

                         val jsonString = String(response.data)
                         loginData = Gson().fromJson(jsonString, AcmoInitModel::class.java)
                         publisherUserID = loginData.data.user.publisherUserId
                         preferences.edit().putString(AcmoKeyNames.USER_ID, publisherUserID).apply()
                         newUser = loginData.data.newRegisteredUser
                         val usageStatsController = AcmoUsageStatsController()
                         usageStatsController.saveUsageStats()
                     }
                     is Result.Failure -> {
                         // Handle error
                     }

                 }

             }

         }catch (e: Exception) {
         }
    }


     fun showOffers() {
         GlobalScope.launch {
             initializationWait?.join()
             val intent = Intent(context, TyradsWebview::class.java)
             context.startActivity(intent)
         }
    }


    @Composable
    fun Dialog(content: @Composable () -> Unit) {
        content()
    }


}