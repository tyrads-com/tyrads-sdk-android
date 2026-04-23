package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models

import com.google.gson.Gson
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Extended unit tests for [LimitedEventsModel] sub-models not covered in [LimitedEventsModelTest]:
 * - [LimitedEventApp] — defaults and field setting
 * - [Currency] — defaults and field setting
 * - [Campaign] — additional fields (sortingScore, createdOn, expiredOn, app, currency, etc.)
 * - [GroupData] — list operations
 * - GSON serialization of newly covered fields
 */
class LimitedEventsModelExtendedTest {

    private val gson = Gson()

    // ============================================================
    // LimitedEventApp
    // ============================================================

    @Test
    fun limitedEventApp_defaultValues_areNull() {
        val app = LimitedEventApp()
        assertNull(app.id)
        assertNull(app.title)
        assertNull(app.packageName)
        assertNull(app.rating)
        assertNull(app.shortDescription)
        assertNull(app.store)
        assertNull(app.storeCategory)
        assertNull(app.previewUrl)
        assertNull(app.thumbnail)
        assertNull(app.confidenceScore)
        assertNull(app.securityLabel)
    }

    @Test
    fun limitedEventApp_withValues_setsCorrectly() {
        val app = LimitedEventApp(
            id = 101,
            title = "Clash of Clans",
            packageName = "com.supercell.clashofclans",
            rating = 4.7,
            shortDescription = "Epic strategy game",
            store = "Play Store",
            storeCategory = "Strategy",
            previewUrl = "https://play.google.com/store/apps/details?id=com.supercell.clashofclans",
            thumbnail = "https://cdn.tyrads.com/thumb.png",
            confidenceScore = 0.95,
            securityLabel = "safe"
        )
        assertEquals(101, app.id)
        assertEquals("Clash of Clans", app.title)
        assertEquals("com.supercell.clashofclans", app.packageName)
        assertEquals(4.7, app.rating!!, 0.001)
        assertEquals("Epic strategy game", app.shortDescription)
        assertEquals("Play Store", app.store)
        assertEquals("Strategy", app.storeCategory)
        assertEquals("https://play.google.com/store/apps/details?id=com.supercell.clashofclans", app.previewUrl)
        assertEquals("https://cdn.tyrads.com/thumb.png", app.thumbnail)
        assertEquals(0.95, app.confidenceScore!!, 0.001)
        assertEquals("safe", app.securityLabel)
    }

    @Test
    fun limitedEventApp_gsonRoundTrip_preservesFields() {
        val original = LimitedEventApp(
            id = 55,
            title = "TestApp",
            rating = 3.8,
            confidenceScore = 0.8
        )
        val json = gson.toJson(original)
        val deserialized = gson.fromJson(json, LimitedEventApp::class.java)
        assertEquals(original.id, deserialized.id)
        assertEquals(original.title, deserialized.title)
        assertEquals(original.rating, deserialized.rating)
        assertEquals(original.confidenceScore, deserialized.confidenceScore)
    }

    // ============================================================
    // Currency
    // ============================================================

    @Test
    fun currency_defaultValues_areNull() {
        val currency = Currency()
        assertNull(currency.name)
        assertNull(currency.symbol)
        assertNull(currency.adUnitName)
        assertNull(currency.adUnitCurrencyName)
        assertNull(currency.adUnitCurrencyConversion)
        assertNull(currency.adUnitCurrencyIcon)
    }

    @Test
    fun currency_withValues_setsCorrectly() {
        val currency = Currency(
            name = "Coins",
            symbol = "C",
            adUnitName = "Gold",
            adUnitCurrencyName = "TyrCoins",
            adUnitCurrencyConversion = 100,
            adUnitCurrencyIcon = "https://cdn.tyrads.com/coin.png"
        )
        assertEquals("Coins", currency.name)
        assertEquals("C", currency.symbol)
        assertEquals("Gold", currency.adUnitName)
        assertEquals("TyrCoins", currency.adUnitCurrencyName)
        assertEquals(100, currency.adUnitCurrencyConversion)
        assertEquals("https://cdn.tyrads.com/coin.png", currency.adUnitCurrencyIcon)
    }

    @Test
    fun currency_gsonRoundTrip_preservesFields() {
        val original = Currency(name = "Gems", symbol = "G", adUnitCurrencyConversion = 50)
        val json = gson.toJson(original)
        val deserialized = gson.fromJson(json, Currency::class.java)
        assertEquals(original.name, deserialized.name)
        assertEquals(original.symbol, deserialized.symbol)
        assertEquals(original.adUnitCurrencyConversion, deserialized.adUnitCurrencyConversion)
    }

    // ============================================================
    // Campaign — additional fields
    // ============================================================

    @Test
    fun campaign_additionalFields_defaultValues_areNull() {
        val campaign = Campaign()
        assertNull(campaign.campaignDescription)
        assertNull(campaign.createdOn)
        assertNull(campaign.sortingScore)
        assertNull(campaign.expiredOn)
        assertNull(campaign.app)
        assertNull(campaign.currency)
        assertNull(campaign.isRetryDownload)
        assertNull(campaign.capReached)
        assertNull(campaign.group)
        assertNull(campaign.premium)
        assertNull(campaign.isOldUser)
        assertNull(campaign.campaignEventSummary)
    }

    @Test
    fun campaign_sortingScore_setsCorrectly() {
        val campaign = Campaign(sortingScore = 98.5)
        assertEquals(98.5, campaign.sortingScore!!, 0.001)
    }

    @Test
    fun campaign_withApp_setsCorrectly() {
        val app = LimitedEventApp(id = 1, title = "My Game")
        val campaign = Campaign(app = app)
        assertEquals(1, campaign.app?.id)
        assertEquals("My Game", campaign.app?.title)
    }

    @Test
    fun campaign_withCurrency_setsCorrectly() {
        val currency = Currency(name = "Coins", adUnitCurrencyConversion = 10)
        val campaign = Campaign(currency = currency)
        assertEquals("Coins", campaign.currency?.name)
        assertEquals(10, campaign.currency?.adUnitCurrencyConversion)
    }

    @Test
    fun campaign_premium_setsCorrectly() {
        val campaign = Campaign(premium = true)
        assertTrue(campaign.premium == true)
    }

    @Test
    fun campaign_capReached_setsCorrectly() {
        val campaign = Campaign(capReached = true)
        assertTrue(campaign.capReached == true)
    }

    @Test
    fun campaign_fullGsonRoundTrip_preservesFields() {
        val original = Campaign(
            campaignId = 42,
            campaignName = "Clash",
            sortingScore = 95.0,
            status = "active",
            isInstalled = true,
            premium = false
        )
        val json = gson.toJson(original)
        val deserialized = gson.fromJson(json, Campaign::class.java)
        assertEquals(original.campaignId, deserialized.campaignId)
        assertEquals(original.campaignName, deserialized.campaignName)
        assertEquals(original.sortingScore, deserialized.sortingScore)
        assertEquals(original.status, deserialized.status)
        assertEquals(original.isInstalled, deserialized.isInstalled)
        assertEquals(original.premium, deserialized.premium)
    }

    // ============================================================
    // GroupData — list operations
    // ============================================================

    @Test
    fun groupData_withMultipleCampaigns_setsCorrectly() {
        val campaigns = listOf(
            Campaign(campaignId = 1, campaignName = "Game 1"),
            Campaign(campaignId = 2, campaignName = "Game 2"),
            Campaign(campaignId = 3, campaignName = "Game 3")
        )
        val group = GroupData(groupName = "hotdeals", campaigns = campaigns)
        assertEquals("hotdeals", group.groupName)
        assertEquals(3, group.campaigns?.size)
    }

    @Test
    fun groupData_emptyCampaigns_returnsEmptyList() {
        val group = GroupData(groupName = "featured", campaigns = emptyList())
        assertTrue(group.campaigns?.isEmpty() == true)
    }

    // ============================================================
    // LimitedTimeEvent — additional fields
    // ============================================================

    @Test
    fun limitedTimeEvent_additionalFields_defaultValues_areNull() {
        val event = LimitedTimeEvent()
        assertNull(event.identifier)
        assertNull(event.eventDescription)
        assertNull(event.eventCategory)
        assertNull(event.payoutAmount)
        assertNull(event.payoutTypeId)
        assertNull(event.payoutType)
        assertNull(event.maxTime)
        assertNull(event.maxTimeMetric)
        assertNull(event.maxTimeRemainSeconds)
        assertNull(event.enforceMaxTimeCompletion)
        assertNull(event.isLimitedTimeEvent)
        assertNull(event.limitedTimeEventRemainingSeconds)
        assertNull(event.isTicketSubmitted)
        assertNull(event.count)
        assertNull(event.limit)
    }

    @Test
    fun limitedTimeEvent_payoutAmount_setsCorrectly() {
        val event = LimitedTimeEvent(payoutAmount = 5.50, payoutAmountConverted = 550)
        assertEquals(5.50, event.payoutAmount!!, 0.001)
        assertEquals(550, event.payoutAmountConverted)
    }

    @Test
    fun limitedTimeEvent_maxTime_setsCorrectly() {
        val event = LimitedTimeEvent(maxTime = 30, maxTimeMetric = "days", maxTimeRemainSeconds = 2592000.0)
        assertEquals(30, event.maxTime)
        assertEquals("days", event.maxTimeMetric)
        assertEquals(2592000.0, event.maxTimeRemainSeconds!!, 0.001)
    }

    @Test
    fun limitedTimeEvent_isLimitedTimeEvent_setsCorrectly() {
        val event = LimitedTimeEvent(isLimitedTimeEvent = true, limitedTimeEventRemainingSeconds = 86400)
        assertTrue(event.isLimitedTimeEvent == true)
        assertEquals(86400, event.limitedTimeEventRemainingSeconds)
    }

    @Test
    fun limitedTimeEvent_countAndLimit_setsCorrectly() {
        val event = LimitedTimeEvent(count = 2, limit = 5)
        assertEquals(2, event.count)
        assertEquals(5, event.limit)
    }

    @Test
    fun limitedTimeEvent_isTicketSubmitted_setsCorrectly() {
        val event = LimitedTimeEvent(isTicketSubmitted = true)
        assertTrue(event.isTicketSubmitted == true)
    }
}
