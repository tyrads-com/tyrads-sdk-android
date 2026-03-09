package com.tyrads.sdk.acmo.modules.input_models

import com.google.gson.annotations.SerializedName

data class AcmoOffersResponseModel(
    val data: List<AcmoOffersModel>
)

data class AcmoOffersModel(
    val campaignId: Int,
    val campaignName: String,
    val campaignDescription: String = "",
    val campaignType: String = "",
    val campaignStatus: String = "",
    @SerializedName("campaignPremium")
    val premium: Boolean = false,
    val app: App,
    val availableCurrencies: Map<String, AvailableCurrency> = emptyMap(),
    val payoutSummary: Map<String, PayoutSummary> = emptyMap(),
    val tracking: Tracking,
    val targeting: Targeting,
    val creative: Creative,
    val validity: Validity = Validity(),
    val hasPlaytimeEvents: Boolean = false,
    val sortingScore: Double = 0.0,
) {
    val isRetryDownload: Boolean get() = validity.isRetryDownload
    val isInstalled: Boolean get() = validity.isInstalled

    // Returns the first available currency, or a default empty one
    val currency: AvailableCurrency
        get() = availableCurrencies.values.firstOrNull() ?: AvailableCurrency()

    // Returns the payout for the first available currency, or a default empty one
    val campaignPayout: PayoutSummary
        get() = availableCurrencies.keys.firstOrNull()
            ?.let { payoutSummary[it] } ?: PayoutSummary()
}

data class Validity(
    val isRetryDownload: Boolean = false,
    val isActivated: Boolean = false,
    val isOldUser: Boolean = false,
    val expiredOn: String? = null,
    val expiredInSeconds: Long? = null,
    val isInstalled: Boolean = false
)

data class AvailableCurrency(
    val currencyId: Int = 0,
    val currencyIcon: String = "",
    val currencyName: String = ""
) {
    // Compatibility shim so existing UI code using currency.adUnitCurrencyIcon still compiles
    val adUnitCurrencyIcon: String get() = currencyIcon
    val adUnitCurrencyName: String get() = currencyName
}

data class PayoutSummary(
    val totalPayoutConverted: Double = 0.0,
    val totalPlayablePayoutConverted: Double = 0.0,
    val totalMicrochargePayoutConverted: Double = 0.0
)

data class Creative(
    val creativeUrl: String = "",
    val creativePacks: List<CreativePacks>
)

data class CreativePacks(
    val creatives: List<Creatives?> = emptyList()
)

data class Creatives(
    val fileUrl: String = ""
)

data class Targeting(
    val os: String? = null,
    val targetingType: String = "",
    val reward: Reward? = null
)

data class Tracking(
    val attributionTool: String? = null,
    val clickUrl: String? = null,
    val impressionUrl: String? = null,
    val s2sClickUrl: String? = null
)

data class App(
    val id: Int = 0,
    val title: String = "",
    val packageName: String = "",
    val shortDescription: String = "",
    val store: String = "",
    val storeCategory: String = "",
    val previewUrl: String = "",
    val thumbnail: String = ""
)

data class Reward(
    val rewardDifficulty: String = ""
)