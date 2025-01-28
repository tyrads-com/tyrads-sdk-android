package com.tyrads.sdk.acmo.modules.input_models


// API Response Data Classes
data class ApiResponse(
    val data: List<Campaign>,
    val message: String
)

data class Campaign(
    val campaignId: Int,
    val campaignName: String,
    val app: App,
    val creative: Creative,
    val campaignPayout: CampaignPayout
)

data class App(
    val id: Int,
    val title: String,
    val thumbnail: String
)

data class Creative(
    val creativePacks: List<CreativePack>
)

data class CreativePack(
    val creativePackName: String,
    val creatives: List<Creative2>
)

data class Creative2(
    val fileUrl: String
)

data class CampaignPayout(
    val totalEvents: Int,
    val totalPayout: Double,
    val totalPayoutConverted: Double
)

// Unified Banner data model
data class BannerData(
    val campaignId: Int,
    val appId: Int,
    val title: String,
    val creativePackName: String,
    val fileUrl: String,
    val points: String,
    val rewards: String,
    val thumbnail: String,
)
