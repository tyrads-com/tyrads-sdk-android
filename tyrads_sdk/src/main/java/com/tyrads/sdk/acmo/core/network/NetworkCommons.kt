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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tyrads.sdk.acmo.modules.input_models.AcmoOfferCurrencySaleModel
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersModel
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersResponseModel
import com.tyrads.sdk.acmo.modules.input_models.CurrencySales

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
                    request.headers["X-Secure-Mode"] = if(Tyrads.getInstance().isSecure) "BASIC" else "PLAIN"
                    request.headers["X-Play-Integrity"] =
                        sharedPreferences.getString(AcmoKeyNames.PLAY_INTEGRITY_TOKEN, "") ?: ""
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

    suspend fun isNewUser(): Boolean = withContext(Dispatchers.Default) {
        val url =
            "${AcmoConfig.BASE_URL}${AcmoEndpointNames.CHECK_PROFILE_COMPLETION}?lang=${Tyrads.getInstance().currentLanguageCode.value}"
        try {
            val result = Fuel.get(url)
                .awaitObject(object : ResponseDeserializable<JsonObject> {
                    override fun deserialize(content: String): JsonObject {
                        return JsonParser.parseString(content).asJsonObject
                    }
                })
            val data = result["data"].asJsonObject
            val isCompleted = data["age"].asBoolean && data["gender"].asBoolean
            !isCompleted
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchCampaigns(langCode: String): List<AcmoOffersModel> = withContext(Dispatchers.IO) {
        val url = "${AcmoConfig.BASE_URL}${AcmoEndpointNames.OFFERS}?lang=$langCode"

        try {
            val result = Fuel.get(url)
                .awaitObject(object : ResponseDeserializable<AcmoOffersResponseModel> {
                    override fun deserialize(content: String): AcmoOffersResponseModel {
                        return Gson().fromJson(content, AcmoOffersResponseModel::class.java)
                    }
                })

            result.data
                .sortedWith(compareByDescending<AcmoOffersModel> { it.premium }
                    .thenByDescending { it.sortingScore })
                .filter { it.campaignPayout.totalPlayablePayoutConverted > 0 }
                .take(5)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchCurrencySale(langCode: String): CurrencySales? = withContext(Dispatchers.IO) {
        val url = "${AcmoConfig.BASE_URL}${AcmoEndpointNames.ENGAGEMENT}?lang=$langCode"

        try {
            val result = Fuel.get(url)
                .awaitObject(object : ResponseDeserializable<AcmoOfferCurrencySaleModel> {
                    override fun deserialize(content: String): AcmoOfferCurrencySaleModel {
                        return Gson().fromJson(content, AcmoOfferCurrencySaleModel::class.java)
                    }
                })

            result.data?.currencySales
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchActiveOffersSummary(langCode: String): Int = withContext(Dispatchers.IO) {
        val url = "${AcmoConfig.BASE_URL}${AcmoEndpointNames.OFFER_SUMMARY}?lang=$langCode"
//        val url = "${AcmoConfig.BASE_URL}${AcmoEndpointNames.ACTIVE_OFFERS}/${AcmoEndpointNames.OFFER_SUMMARY}?lang=$langCode"

        try {
            val result = Fuel.get(url)
                .awaitObject(object : ResponseDeserializable<JsonObject> {
                    override fun deserialize(content: String): JsonObject {
                        return JsonParser.parseString(content).asJsonObject
                    }
                })

            result["data"].asJsonObject["activeCampaignCount"].asInt
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun activateOffer(id: String) = withContext(Dispatchers.IO) {
        val url = "${AcmoConfig.BASE_URL}${AcmoEndpointNames.OFFERS}/active/$id"

        try {
            val ( _, _, _ ) = Fuel.post(url).response()
        } catch (e: Exception) {
            throw e
        }
    }
}