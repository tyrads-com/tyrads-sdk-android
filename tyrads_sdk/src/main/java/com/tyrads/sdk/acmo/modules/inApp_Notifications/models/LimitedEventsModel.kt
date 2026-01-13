package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models

data class AcmoLimitedEventsModel(
    val data: List<GroupData>? = null,
    val message: String? = null
)

data class GroupData(
    val groupName: String? = null,
    val campaigns: List<Campaign>? = null
)

data class Campaign(
    val campaignId: Int? = null,
    val campaignName: String? = null,
    val campaignDescription: String? = null,
    val createdOn: String? = null,
    val sortingScore: Double? = null,
    val status: String? = null,
    val expiredOn: String? = null,
    val app: LimitedEventApp? = null,
    val currency: Currency? = null,
    val isRetryDownload: Boolean? = null,
    val capReached: Boolean? = null,
    val group: String? = null,
    val premium: Boolean? = null,
    val isOldUser: Boolean? = null,
    val isInstalled: Boolean? = null,
    val campaignEventSummary: CampaignEventSummary? = null,
    val limitedTimeEvents: List<LimitedTimeEvent>? = null
)

data class LimitedEventApp(
    val id: Int? = null,
    val title: String? = null,
    val packageName: String? = null,
    val rating: Double? = null,
    val shortDescription: String? = null,
    val store: String? = null,
    val storeCategory: String? = null,
    val previewUrl: String? = null,
    val thumbnail: String? = null,
    val confidenceScore: Double? = null,
    val securityLabel: String? = null
)

data class Currency(
    val name: String? = null,
    val symbol: String? = null,
    val adUnitName: String? = null,
    val adUnitCurrencyName: String? = null,
    val adUnitCurrencyConversion: Int? = null,
    val adUnitCurrencyIcon: String? = null
)

data class CampaignEventSummary(
    val playableEventCountAvailable: Int? = null,
    val playableEventCountCompleted: Int? = null,
    val playableEventCountTotal: Int? = null
)

data class LimitedTimeEvent(
    val id: Int? = null,
    val conversionStatus: String? = null,
    val identifier: String? = null,
    val eventName: String? = null,
    val eventDescription: String? = null,
    val eventCategory: String? = null,
    val payoutAmount: Double? = null,
    val payoutAmountConverted: Int? = null,
    val payoutTypeId: Int? = null,
    val payoutType: String? = null,
    val allowDuplicateEvents: Boolean? = null,
    val maxTime: Int? = null,
    val maxTimeMetric: String? = null,
    val maxTimeRemainSeconds: Double? = null,
    val enforceMaxTimeCompletion: Boolean? = null,
    val isLimitedTimeEvent: Boolean? = null,
    val limitedTimeEventRemainingSeconds: Int? = null,
    val isTicketSubmitted: Boolean? = null,
    val dailyCount: Int? = null,
    val dailyLimit: Int? = null,
    val count: Int? = null,
    val limit: Int? = null
)
