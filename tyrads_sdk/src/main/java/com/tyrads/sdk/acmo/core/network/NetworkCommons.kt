package com.tyrads.sdk

import AcmoConfig
import AcmoEndpointNames
import AcmoKeyNames
import android.util.Log
import androidx.annotation.Keep
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.gson.responseObject
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersModel
import com.tyrads.sdk.acmo.modules.input_models.BannerData
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
                    request.header("X-SDK-Version", AcmoConfig.SDK_VERSION)
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
                    Tyrads.getInstance().log("Request headers set: ${request}")
                }
                next(request)
            }
        }

        FuelManager.instance.addResponseInterceptor { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->
                try {
                    Tyrads.getInstance().log("Response: ${response}")
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

    fun fetchCampaigns(onSuccess: (List<BannerData>) -> Unit, onError: (Exception) -> Unit, langCode: String) {
        val url = "${AcmoConfig.BASE_URL}campaigns?lang=$langCode"
        Fuel.get(url)
            .responseObject<AcmoOffersModel> { _, _, result ->
                result.fold(
                    success = { response ->
                        Log.i("offers response", response.data.toString())
                        val banners = response.data.map { campaign ->
                            BannerData(
                                campaignId = campaign.campaignId,
                                appId = campaign.app.id,
                                title = campaign.app.title,
                                creativePackName = campaign.creative.creativePacks.firstOrNull()?.creativePackName
                                    ?: "",
                                fileUrl = campaign.creative.creativePacks.firstOrNull()?.creatives?.firstOrNull()?.fileUrl
                                    ?: "",
                                points = campaign.campaignPayout.totalPayoutConverted,
                                rewards = campaign.campaignPayout.totalEvents,
                                currency = campaign.currency,
                                thumbnail = campaign.app.thumbnail,
                                premium = campaign.premium,
                                sortingScore = campaign.sortingScore
                            )
                        }
                        val hotOffers = banners
                            .sortedWith(compareByDescending<BannerData> { it.premium }.thenByDescending { it.sortingScore })
                            .filter { it.points > 0 }
                            .take(5)

                        onSuccess(hotOffers)
                    },
                    failure = { error -> onError(error) }
                )
            }
    }
}