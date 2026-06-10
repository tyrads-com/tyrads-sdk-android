package com.tyrads.sdk.acmo.modules.input_models

import com.google.gson.Gson
import com.tyrads.sdk.acmo.modules.input_models.AcmoOfferCurrencySaleModel
import com.tyrads.sdk.acmo.modules.input_models.CurrencySales
import com.tyrads.sdk.acmo.modules.input_models.EngagementData
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [CurrencySales] and [AcmoOfferCurrencySaleModel] data classes.
 */
class CurrencySalesTest {

    private val gson = Gson()

    // ---- Default values ----

    @Test
    fun currencySales_defaultValues_areNull() {
        val cs = CurrencySales()
        assertNull(cs.name)
        assertNull(cs.multiplier)
        assertNull(cs.bannerUrl)
        assertNull(cs.dateStart)
        assertNull(cs.dateEnd)
        assertNull(cs.remainingTimeSeconds)
    }

    @Test
    fun currencySales_withAllFields_setsCorrectly() {
        val cs = CurrencySales(
            name = "Double Coins",
            multiplier = 2.0,
            bannerUrl = "https://example.com/banner.png",
            dateStart = "2024-01-01",
            dateEnd = "2024-01-07",
            remainingTimeSeconds = 3600
        )
        assertEquals("Double Coins", cs.name)
        assertEquals(2.0, cs.multiplier!!, 0.001)
        assertEquals("https://example.com/banner.png", cs.bannerUrl)
        assertEquals("2024-01-01", cs.dateStart)
        assertEquals("2024-01-07", cs.dateEnd)
        assertEquals(3600, cs.remainingTimeSeconds)
    }

    // ---- Gson deserialization ----

    @Test
    fun currencySales_gsonRoundTrip_preservesAllFields() {
        val original = CurrencySales(
            name = "Triple Coins",
            multiplier = 3.0,
            bannerUrl = "https://banner.url/img.png",
            dateStart = "2024-02-01",
            dateEnd = "2024-02-14",
            remainingTimeSeconds = 7200
        )
        val json = gson.toJson(original)
        val deserialized = gson.fromJson(json, CurrencySales::class.java)

        assertEquals(original.name, deserialized.name)
        assertEquals(original.multiplier, deserialized.multiplier)
        assertEquals(original.bannerUrl, deserialized.bannerUrl)
        assertEquals(original.dateStart, deserialized.dateStart)
        assertEquals(original.dateEnd, deserialized.dateEnd)
        assertEquals(original.remainingTimeSeconds, deserialized.remainingTimeSeconds)
    }

    @Test
    fun acmoOfferCurrencySaleModel_fromJson_parsesCorrectly() {
        // Note: the field in JSON is "CurrencySales" (capital C) per @SerializedName annotation
        val json = """
            {
                "data": {
                    "CurrencySales": {
                        "name": "Gold Rush",
                        "multiplier": 1.5,
                        "bannerUrl": "https://cdn.tyrads.com/banner.jpg",
                        "dateStart": "2024-03-01",
                        "dateEnd": "2024-03-10",
                        "remainingTimeSeconds": 86400
                    }
                },
                "message": "Success"
            }
        """.trimIndent()

        val model = gson.fromJson(json, AcmoOfferCurrencySaleModel::class.java)
        assertNotNull(model)
        assertEquals("Success", model.message)
        assertNotNull(model.data)
        assertNotNull(model.data?.currencySales)
        assertEquals("Gold Rush", model.data?.currencySales?.name)
        assertEquals(1.5, model.data?.currencySales?.multiplier!!, 0.001)
    }

    @Test
    fun acmoOfferCurrencySaleModel_nullData_parsesGracefully() {
        val json = """{"data": null, "message": "Empty"}"""
        val model = gson.fromJson(json, AcmoOfferCurrencySaleModel::class.java)
        assertNull(model.data)
        assertEquals("Empty", model.message)
    }

    // ---- Equality ----

    @Test
    fun identicalCurrencySales_areEqual() {
        val cs1 = CurrencySales(name = "Test", multiplier = 2.0)
        val cs2 = CurrencySales(name = "Test", multiplier = 2.0)
        assertEquals(cs1, cs2)
    }

    @Test
    fun differentCurrencySales_areNotEqual() {
        val cs1 = CurrencySales(name = "Test", multiplier = 2.0)
        val cs2 = CurrencySales(name = "Test", multiplier = 3.0)
        assertNotEquals(cs1, cs2)
    }
}
