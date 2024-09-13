package com.tyrads.sdk


import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.runBlocking

@Keep
class NetworkCommons {
    private var isDialogOpen = false

    init {
        FuelManager.instance.baseHeaders = mapOf(
            "Content-Type" to "application/json"
        )
        FuelManager.instance.baseParams = listOf()
        FuelManager.instance.basePath = AcmoConfig.BASE_URL
        FuelManager.instance.timeoutInMillisecond = 300000 // 5 minutes
        FuelManager.instance.timeoutReadInMillisecond = 300000 // 5 minutes

        Tyrads.getInstance().log("NetworkCommons initialized with base URL: ${AcmoConfig.BASE_URL}")

        FuelManager.instance.addRequestInterceptor { next: (Request) -> Request ->
            { request: Request ->
                runBlocking {
                    request.header("X-SDK-Platform", "Android")
                    request.header("X-SDK-Version", "v0.1.0")
                    request.header("Content-Type", "application/json")
                    val sharedPreferences = Tyrads.getInstance().preferences

                    if (!request.url.path.endsWith(AcmoEndpointNames.INITIALIZE)) {
                        request.headers["X-User-ID"] =
                            sharedPreferences.getString(AcmoKeyNames.USER_ID, null) ?: ""
                    }
                    request.headers["X-API-Key"] =
                        sharedPreferences.getString(AcmoKeyNames.API_KEY, null) ?: ""
                    request.headers["X-API-Secret"] =
                        sharedPreferences.getString(AcmoKeyNames.API_SECRET, null) ?: ""
                    request.headers["X-SDK-Platform"] = AcmoConfig.SDK_PLATFORM
                    request.headers["X-SDK-Version"] = AcmoConfig.SDK_VERSION
                    Tyrads.getInstance().log("Request headers set: ${request.headers}")
                }
                next(request)
            }
        }

        FuelManager.instance.addResponseInterceptor { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->
                try {
                    when (response.statusCode) {
                        in 200..299 -> next(request, response)
                        else -> throw FuelError.wrap(Exception("Network error: ${response.statusCode}"))
                    }
                } catch (e: Exception) {
                    Tyrads.getInstance().log("Network error: ${e.message}", Log.ERROR)
                    throw e
                }
            }
        }
    }
}



