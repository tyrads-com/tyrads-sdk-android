package com.tyrads.sdk.acmo.modules.input_models

import com.google.gson.annotations.SerializedName

data class AcmoOfferCurrencySaleModel(
    val data: Data? = null,
    val message: String? = null
)

data class Data(
    @SerializedName("CurrencySales")
    val currencySales: CurrencySales? = null
)

data class CurrencySales(
    val name: String? = null,
    val multiplier: Double? = null,
    val bannerUrl: String? = null,
    val dateStart: String? = null,
    val dateEnd: String? = null,
    val remainingTimeSeconds: Int? = null
)
