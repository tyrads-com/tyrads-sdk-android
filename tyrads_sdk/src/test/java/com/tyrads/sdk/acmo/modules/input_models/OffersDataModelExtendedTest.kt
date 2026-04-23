package com.tyrads.sdk.acmo.modules.input_models

import com.google.gson.Gson
import com.tyrads.sdk.acmo.modules.input_models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Extended unit tests for offer data models not covered in [OffersDataModelTest]:
 * - [AcmoOffersResponseModel] — GSON parsing, list size
 * - [Targeting] defaults and reward
 * - [Tracking] defaults
 * - [Creative] and [CreativePacks]
 * - [Reward] defaults
 * - [AcmoOffersModel] additional computed properties and field coverage
 */
class OffersDataModelExtendedTest {

    private val gson = Gson()

    // ============================================================
    // AcmoOffersResponseModel
    // ============================================================

    @Test
    fun acmoOffersResponseModel_fromJson_parsesDataList() {
        val json = """
            {
                "data": [
                    {
                        "campaignId": 10,
                        "campaignName": "Game A",
                        "app": {},
                        "tracking": {},
                        "targeting": {},
                        "creative": { "creativeUrl": "", "creativePacks": [] }
                    },
                    {
                        "campaignId": 20,
                        "campaignName": "Game B",
                        "app": {},
                        "tracking": {},
                        "targeting": {},
                        "creative": { "creativeUrl": "", "creativePacks": [] }
                    }
                ]
            }
        """.trimIndent()

        val model = gson.fromJson(json, AcmoOffersResponseModel::class.java)
        assertNotNull(model)
        assertEquals(2, model.data.size)
        assertEquals(10, model.data[0].campaignId)
        assertEquals("Game A", model.data[0].campaignName)
        assertEquals(20, model.data[1].campaignId)
    }

    @Test
    fun acmoOffersResponseModel_emptyData_parsesCorrectly() {
        val json = """{"data": []}"""
        val model = gson.fromJson(json, AcmoOffersResponseModel::class.java)
        assertNotNull(model)
        assertTrue(model.data.isEmpty())
    }

    // ============================================================
    // AcmoOffersModel — default fields
    // ============================================================

    @Test
    fun acmoOffersModel_defaultOptionalFields_areCorrect() {
        val offer = AcmoOffersModel(
            campaignId = 1,
            campaignName = "Test",
            app = App(),
            tracking = Tracking(),
            targeting = Targeting(),
            creative = Creative(creativeUrl = "", creativePacks = emptyList())
        )
        assertEquals("", offer.campaignDescription)
        assertEquals("", offer.campaignType)
        assertEquals("", offer.campaignStatus)
        assertFalse(offer.premium)
        assertFalse(offer.hasPlaytimeEvents)
        assertEquals(0.0, offer.sortingScore, 0.001)
    }

    @Test
    fun acmoOffersModel_premiumFlag_setCorrectly() {
        val offer = AcmoOffersModel(
            campaignId = 2,
            campaignName = "Premium Game",
            premium = true,
            app = App(),
            tracking = Tracking(),
            targeting = Targeting(),
            creative = Creative(creativeUrl = "", creativePacks = emptyList())
        )
        assertTrue(offer.premium)
    }

    @Test
    fun acmoOffersModel_hasPlaytimeEvents_setCorrectly() {
        val offer = AcmoOffersModel(
            campaignId = 3,
            campaignName = "Playtime Game",
            hasPlaytimeEvents = true,
            app = App(),
            tracking = Tracking(),
            targeting = Targeting(),
            creative = Creative(creativeUrl = "", creativePacks = emptyList())
        )
        assertTrue(offer.hasPlaytimeEvents)
    }

    @Test
    fun acmoOffersModel_sortingScore_setsCorrectly() {
        val offer = AcmoOffersModel(
            campaignId = 4,
            campaignName = "Sorted",
            sortingScore = 99.5,
            app = App(),
            tracking = Tracking(),
            targeting = Targeting(),
            creative = Creative(creativeUrl = "", creativePacks = emptyList())
        )
        assertEquals(99.5, offer.sortingScore, 0.001)
    }

    // ============================================================
    // Targeting
    // ============================================================

    @Test
    fun targeting_defaults_areCorrect() {
        val targeting = Targeting()
        assertNull(targeting.os)
        assertEquals("", targeting.targetingType)
        assertNull(targeting.reward)
    }

    @Test
    fun targeting_withOs_setsCorrectly() {
        val targeting = Targeting(os = "Android", targetingType = "cpi")
        assertEquals("Android", targeting.os)
        assertEquals("cpi", targeting.targetingType)
    }

    @Test
    fun targeting_withReward_setsCorrectly() {
        val reward = Reward(rewardDifficulty = "hard")
        val targeting = Targeting(reward = reward)
        assertEquals("hard", targeting.reward?.rewardDifficulty)
    }

    // ============================================================
    // Tracking
    // ============================================================

    @Test
    fun tracking_defaults_areNull() {
        val tracking = Tracking()
        assertNull(tracking.attributionTool)
        assertNull(tracking.clickUrl)
        assertNull(tracking.impressionUrl)
        assertNull(tracking.s2sClickUrl)
    }

    @Test
    fun tracking_withValues_setsCorrectly() {
        val tracking = Tracking(
            attributionTool = "AppsFlyer",
            clickUrl = "https://click.url",
            impressionUrl = "https://impression.url",
            s2sClickUrl = "https://s2s.url"
        )
        assertEquals("AppsFlyer", tracking.attributionTool)
        assertEquals("https://click.url", tracking.clickUrl)
        assertEquals("https://impression.url", tracking.impressionUrl)
        assertEquals("https://s2s.url", tracking.s2sClickUrl)
    }

    // ============================================================
    // Creative & CreativePacks
    // ============================================================

    @Test
    fun creative_setsFieldsCorrectly() {
        val creative = Creative(
            creativeUrl = "https://cdn.tyrads.com/img.png",
            creativePacks = listOf(CreativePacks(creatives = emptyList()))
        )
        assertEquals("https://cdn.tyrads.com/img.png", creative.creativeUrl)
        assertEquals(1, creative.creativePacks.size)
    }

    @Test
    fun creativePacks_defaultCreatives_isEmpty() {
        val pack = CreativePacks()
        assertTrue(pack.creatives.isEmpty())
    }

    @Test
    fun creatives_defaultFileUrl_isEmpty() {
        val c = Creatives()
        assertEquals("", c.fileUrl)
    }

    @Test
    fun creatives_fileUrl_setsCorrectly() {
        val c = Creatives(fileUrl = "https://cdn.tyrads.com/video.mp4")
        assertEquals("https://cdn.tyrads.com/video.mp4", c.fileUrl)
    }

    // ============================================================
    // App — additional fields
    // ============================================================

    @Test
    fun app_additionalFields_defaults_areEmpty() {
        val app = App()
        assertEquals("", app.shortDescription)
        assertEquals("", app.store)
        assertEquals("", app.storeCategory)
        assertEquals("", app.previewUrl)
        assertEquals("", app.thumbnail)
    }

    @Test
    fun app_allFields_setCorrectly() {
        val app = App(
            id = 42,
            title = "Super Game",
            packageName = "com.super.game",
            shortDescription = "Fun game",
            store = "Play Store",
            storeCategory = "Games",
            previewUrl = "https://preview.url",
            thumbnail = "https://thumbnail.url"
        )
        assertEquals(42, app.id)
        assertEquals("Super Game", app.title)
        assertEquals("com.super.game", app.packageName)
        assertEquals("Fun game", app.shortDescription)
        assertEquals("Play Store", app.store)
        assertEquals("Games", app.storeCategory)
        assertEquals("https://preview.url", app.previewUrl)
        assertEquals("https://thumbnail.url", app.thumbnail)
    }

    // ============================================================
    // Reward
    // ============================================================

    @Test
    fun reward_default_isEmpty() {
        val reward = Reward()
        assertEquals("", reward.rewardDifficulty)
    }

    @Test
    fun reward_withDifficulty_setsCorrectly() {
        val reward = Reward(rewardDifficulty = "medium")
        assertEquals("medium", reward.rewardDifficulty)
    }

    // ============================================================
    // Validity — additional fields
    // ============================================================

    @Test
    fun validity_withAllFields_setsCorrectly() {
        val validity = Validity(
            isRetryDownload = true,
            isActivated = true,
            isOldUser = true,
            expiredOn = "2024-12-31",
            expiredInSeconds = 7200L,
            isInstalled = true
        )
        assertTrue(validity.isRetryDownload)
        assertTrue(validity.isActivated)
        assertTrue(validity.isOldUser)
        assertEquals("2024-12-31", validity.expiredOn)
        assertEquals(7200L, validity.expiredInSeconds)
        assertTrue(validity.isInstalled)
    }
}
