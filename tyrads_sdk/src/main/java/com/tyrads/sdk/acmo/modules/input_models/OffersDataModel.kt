package com.tyrads.sdk.acmo.modules.input_models

data class AcmoOffersResponseModel(
    val data: List<AcmoOffersModel>
)

data class AcmoOffersModel(
    val campaignId: Int,
    val campaignName: String,
    val campaignDescription: String = "",
    val active: String = "",
    val status: String = "",
    val app: App,
    val currency: Currency,
    val campaignPayout: CampaignPayout,
    val tracking: Tracking,
    val targeting: Targeting,
    val creative: Creative,
    val hasPlaytimeEvents: Boolean = false,
    val premium: Boolean = false,
    val isRetryDownload: Boolean = false,
    val isInstalled: Boolean = false,
    val sortingScore: Double = 0.0,
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

data class CampaignPayout(
    val totalEvents: Int = 0,
    val totalPayout: Double = 0.0,
    val totalPayoutConverted: Double = 0.0,
    val totalPlayablePayout: Double = 0.0,
    val totalMicrochargePayout: Double = 0.0,
    val totalPlayablePayoutConverted: Double = 0.0,
    val totalMicrochargePayoutConverted: Double = 0.0
)

data class Currency(
    val name: String = "",
    val symbol: String = "",
    val adUnitName: String = "",
    val adUnitCurrencyName: String = "",
    val adUnitCurrencyIcon: String = "",
    val adUnitCurrencyConversion: Double = 0.0
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
