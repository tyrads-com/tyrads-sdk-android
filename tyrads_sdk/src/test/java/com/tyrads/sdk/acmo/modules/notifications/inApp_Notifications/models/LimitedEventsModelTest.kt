package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models

import com.google.gson.Gson
import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [AcmoLimitedEventsModel], [GroupData], [Campaign], [LimitedTimeEvent].
 */
class LimitedEventsModelTest {

    private val gson = Gson()

    // ---- Default values ----

    @Test
    fun limitedTimeEvent_defaultValues_areNull() {
        val event = LimitedTimeEvent()
        assertNull(event.id)
        assertNull(event.conversionStatus)
        assertNull(event.allowDuplicateEvents)
        assertNull(event.dailyCount)
        assertNull(event.dailyLimit)
    }

    @Test
    fun campaign_defaultValues_areNull() {
        val campaign = Campaign()
        assertNull(campaign.campaignId)
        assertNull(campaign.campaignName)
        assertNull(campaign.status)
        assertNull(campaign.isInstalled)
        assertNull(campaign.limitedTimeEvents)
    }

    @Test
    fun groupData_defaultValues_areNull() {
        val group = GroupData()
        assertNull(group.groupName)
        assertNull(group.campaigns)
    }

    @Test
    fun acmoLimitedEventsModel_defaultValues_areNull() {
        val model = AcmoLimitedEventsModel()
        assertNull(model.data)
        assertNull(model.message)
    }

    // ---- Gson deserialization ----

    @Test
    fun limitedTimeEvent_gsonRoundTrip_preservesFields() {
        val original = LimitedTimeEvent(
            id = 101,
            conversionStatus = "pending",
            eventName = "Level 5",
            payoutAmountConverted = 500,
            allowDuplicateEvents = true,
            dailyCount = 2,
            dailyLimit = 3
        )
        val json = gson.toJson(original)
        val deserialized = gson.fromJson(json, LimitedTimeEvent::class.java)

        assertEquals(original.id, deserialized.id)
        assertEquals(original.conversionStatus, deserialized.conversionStatus)
        assertEquals(original.eventName, deserialized.eventName)
        assertEquals(original.payoutAmountConverted, deserialized.payoutAmountConverted)
        assertEquals(original.allowDuplicateEvents, deserialized.allowDuplicateEvents)
        assertEquals(original.dailyCount, deserialized.dailyCount)
        assertEquals(original.dailyLimit, deserialized.dailyLimit)
    }

    @Test
    fun acmoLimitedEventsModel_fromComplexJson_parsesCorrectly() {
        val json = """
            {
                "data": [
                    {
                        "groupName": "hotdeals",
                        "campaigns": [
                            {
                                "campaignId": 999,
                                "campaignName": "Clash of Clans",
                                "status": "active",
                                "isInstalled": true,
                                "limitedTimeEvents": [
                                    {
                                        "id": 1,
                                        "conversionStatus": null,
                                        "allowDuplicateEvents": false,
                                        "eventName": "Reach Level 10",
                                        "dailyCount": 0,
                                        "dailyLimit": 1
                                    }
                                ]
                            }
                        ]
                    }
                ],
                "message": "OK"
            }
        """.trimIndent()

        val model = gson.fromJson(json, AcmoLimitedEventsModel::class.java)

        assertNotNull(model)
        assertEquals("OK", model.message)
        assertEquals(1, model.data?.size)

        val group = model.data?.first()
        assertEquals("hotdeals", group?.groupName)
        assertEquals(1, group?.campaigns?.size)

        val campaign = group?.campaigns?.first()
        assertEquals(999, campaign?.campaignId)
        assertEquals("active", campaign?.status)
        assertTrue(campaign?.isInstalled == true)

        val event = campaign?.limitedTimeEvents?.first()
        assertEquals(1, event?.id)
        assertNull(event?.conversionStatus)
        assertEquals(false, event?.allowDuplicateEvents)
        assertEquals("Reach Level 10", event?.eventName)
    }

    @Test
    fun acmoLimitedEventsModel_emptyData_parsesCorrectly() {
        val json = """{"data": [], "message": "No data"}"""
        val model = gson.fromJson(json, AcmoLimitedEventsModel::class.java)
        assertNotNull(model)
        assertTrue(model.data?.isEmpty() == true)
    }

    // ---- CampaignEventSummary ----

    @Test
    fun campaignEventSummary_defaults_areNull() {
        val summary = CampaignEventSummary()
        assertNull(summary.playableEventCountAvailable)
        assertNull(summary.playableEventCountCompleted)
        assertNull(summary.playableEventCountTotal)
    }

    @Test
    fun campaignEventSummary_withValues_setsCorrectly() {
        val summary = CampaignEventSummary(
            playableEventCountAvailable = 5,
            playableEventCountCompleted = 3,
            playableEventCountTotal = 8
        )
        assertEquals(5, summary.playableEventCountAvailable)
        assertEquals(3, summary.playableEventCountCompleted)
        assertEquals(8, summary.playableEventCountTotal)
    }
}
