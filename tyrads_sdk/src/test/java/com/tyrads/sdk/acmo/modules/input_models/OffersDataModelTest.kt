package com.tyrads.sdk.acmo.modules.input_models

import com.tyrads.sdk.acmo.modules.input_models.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the data model classes:
 * [AcmoOffersModel], [PayoutSummary], [AvailableCurrency], [Validity]
 */
class OffersDataModelTest {

    private fun makeOffer(
        availableCurrencies: Map<String, AvailableCurrency> = emptyMap(),
        payoutSummary: Map<String, PayoutSummary> = emptyMap(),
        validity: Validity = Validity()
    ): AcmoOffersModel = AcmoOffersModel(
        campaignId = 1,
        campaignName = "Test Campaign",
        app = App(),
        tracking = Tracking(),
        targeting = Targeting(),
        creative = Creative(creativeUrl = "", creativePacks = emptyList()),
        availableCurrencies = availableCurrencies,
        payoutSummary = payoutSummary,
        validity = validity
    )

    // ---- currency computed property ----

    @Test
    fun currency_emptyMap_returnsDefaultAvailableCurrency() {
        val offer = makeOffer()
        val currency = offer.currency
        // Should return a default AvailableCurrency with all default values
        assertEquals(0, currency.currencyId)
        assertEquals("", currency.currencyIcon)
        assertEquals("", currency.currencyName)
    }

    @Test
    fun currency_nonEmptyMap_returnsFirstValue() {
        val expected = AvailableCurrency(currencyId = 42, currencyIcon = "icon.png", currencyName = "Coins")
        val offer = makeOffer(
            availableCurrencies = mapOf("USD" to expected)
        )
        assertEquals(expected, offer.currency)
    }

    @Test
    fun currency_adUnitAliases_matchFields() {
        val currency = AvailableCurrency(
            currencyId = 1,
            currencyIcon = "https://icon.url/img.png",
            currencyName = "Gold"
        )
        assertEquals(currency.currencyIcon, currency.adUnitCurrencyIcon)
        assertEquals(currency.currencyName, currency.adUnitCurrencyName)
    }

    // ---- campaignPayout computed property ----

    @Test
    fun campaignPayout_emptyMaps_returnsDefaultPayoutSummary() {
        val offer = makeOffer()
        val payout = offer.campaignPayout
        assertEquals(0.0, payout.totalPayoutConverted, 0.001)
        assertEquals(0.0, payout.totalPlayablePayoutConverted, 0.001)
    }

    @Test
    fun campaignPayout_matchingKey_returnsCorrectSummary() {
        val expectedPayout = PayoutSummary(
            totalPayoutConverted = 100.0,
            totalPlayablePayoutConverted = 75.0,
            totalMicrochargePayoutConverted = 25.0
        )
        val offer = makeOffer(
            availableCurrencies = mapOf("USD" to AvailableCurrency(currencyId = 1)),
            payoutSummary = mapOf("USD" to expectedPayout)
        )
        assertEquals(expectedPayout, offer.campaignPayout)
    }

    @Test
    fun campaignPayout_keyMismatch_returnsDefault() {
        val offer = makeOffer(
            availableCurrencies = mapOf("USD" to AvailableCurrency(currencyId = 1)),
            payoutSummary = mapOf("EUR" to PayoutSummary(totalPayoutConverted = 50.0))
        )
        // The first currency key is "USD" but payout only has "EUR" — should return default
        val payout = offer.campaignPayout
        assertEquals(0.0, payout.totalPayoutConverted, 0.001)
    }

    // ---- isRetryDownload / isInstalled delegates ----

    @Test
    fun isRetryDownload_delegatesToValidity() {
        val offer = makeOffer(validity = Validity(isRetryDownload = true))
        assertTrue(offer.isRetryDownload)
    }

    @Test
    fun isRetryDownload_default_isFalse() {
        val offer = makeOffer()
        assertFalse(offer.isRetryDownload)
    }

    @Test
    fun isInstalled_delegatesToValidity() {
        val offer = makeOffer(validity = Validity(isInstalled = true))
        assertTrue(offer.isInstalled)
    }

    @Test
    fun isInstalled_default_isFalse() {
        val offer = makeOffer()
        assertFalse(offer.isInstalled)
    }

    // ---- Validity defaults ----

    @Test
    fun validity_defaults_areCorrect() {
        val validity = Validity()
        assertFalse(validity.isRetryDownload)
        assertFalse(validity.isActivated)
        assertFalse(validity.isOldUser)
        assertNull(validity.expiredOn)
        assertNull(validity.expiredInSeconds)
        assertFalse(validity.isInstalled)
    }

    // ---- PayoutSummary defaults ----

    @Test
    fun payoutSummary_defaults_areZero() {
        val payout = PayoutSummary()
        assertEquals(0.0, payout.totalPayoutConverted, 0.001)
        assertEquals(0.0, payout.totalPlayablePayoutConverted, 0.001)
        assertEquals(0.0, payout.totalMicrochargePayoutConverted, 0.001)
    }

    // ---- App defaults ----

    @Test
    fun app_defaults_areEmpty() {
        val app = App()
        assertEquals(0, app.id)
        assertEquals("", app.title)
        assertEquals("", app.packageName)
    }
}
