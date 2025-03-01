package com.tyrads.sdk.acmo.modules.input_models


// API Response Data Classes
data class BannerData(
    val campaignId: Int,
    val appId: Int,
    val title: String,
    val creativePackName: String,
    val fileUrl: String,
    val points: Double,
    val currency: Currency,
    val rewards: Int,
    val thumbnail: String,
    val premium: Boolean?,
    val sortingScore: Double
)

data class AcmoOffersModel (
    val data: List<Campaign>,
    val message: String
)

data class Campaign (
    val campaignID: Int,
    val campaignName: String,
    val campaignDescription: String? = null,
    val createdOn: String,
    val sortingScore: Double,
    val status: String,
    val expiredOn: Any? = null,
    val hasPlaytimeEvents: Boolean,
    val capReached: Boolean,
    val premium: Boolean? = null,
    val app: App,
    val currency: Currency,
    val campaignPayout: CampaignPayout,
    val tracking: Tracking,
    val targeting: Targeting,
    val creative: DatumCreative
)

data class App (
    val id: Int,
    val title: String,
    val packageName: String,
    val rating: Double,
    val shortDescription: String,
    val store: String,
    val storeCategory: String,
    val previewUrl: String,
    val thumbnail: String
)

data class CampaignPayout (
    val totalEvents: Int,
    val totalPayout: Double,
    val totalPayoutConverted: Double
)

data class DatumCreative (
    val creativeUrl: String,
    val creativePacks: List<CreativePack>
)

data class CreativePack (
    val creativePackName: String,
    val languageName: String,
    val languageCode: String,
    val creatives: List<CreativeElement>
)

data class CreativeElement (
    val creativeName: String,
    val callToAction: Any? = null,
    val text: Any? = null,
    val byteSize: String,
    val fileUrl: String,
    val duration: Any? = null,
    val creativeType: CreativeType
)

data class CreativeType (
    val name: String,
    val type: String,
    val width: String,
    val height: String,
    val creativeCategoryType: String
)

data class Currency (
    val name: String,
    val symbol: String,
    val adUnitName: String,
    val adUnitCurrencyName: String,
    val adUnitCurrencyConversion: Long,
    val adUnitCurrencyIcon: String
)

data class Targeting (
    val os: String,
    val targetingType: String,
    val reward: Reward? = null
)

data class Reward (
    val rewardDifficulty: String,
    val incentRewardDescription: String
)

data class Tracking (
    val impressionUrl: String,
    val clickUrl: String
)

