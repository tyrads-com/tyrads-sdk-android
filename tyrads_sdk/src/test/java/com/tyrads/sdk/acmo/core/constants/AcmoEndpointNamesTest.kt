package com.tyrads.sdk.acmo.core.constants

import AcmoEndpointNames
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [AcmoEndpointNames] constants.
 * Verifies every API endpoint path is correctly defined.
 */
class AcmoEndpointNamesTest {

    @Test
    fun initialize_isCorrect() {
        assertEquals("initialize", AcmoEndpointNames.INITIALIZE)
    }

    @Test
    fun offers_isCorrect() {
        assertEquals("campaigns", AcmoEndpointNames.OFFERS)
    }

    @Test
    fun activeOffers_isCorrect() {
        assertEquals("campaigns/activated", AcmoEndpointNames.ACTIVE_OFFERS)
    }

    @Test
    fun updateUser_isCorrect() {
        assertEquals("update-user", AcmoEndpointNames.UPDATE_USER)
    }

    @Test
    fun deviceDetails_isCorrect() {
        assertEquals("user-device", AcmoEndpointNames.DEVICE_DETAILS)
    }

    @Test
    fun offerSummary_isCorrect() {
        assertEquals("campaigns/activated/summary", AcmoEndpointNames.OFFER_SUMMARY)
    }

    @Test
    fun engagement_isCorrect() {
        assertEquals("account/engagement", AcmoEndpointNames.ENGAGEMENT)
    }

    @Test
    fun usageStats_isCorrect() {
        assertEquals("usage-stats", AcmoEndpointNames.USAGE_STATS)
    }

    @Test
    fun userActivities_isCorrect() {
        assertEquals("account/activity", AcmoEndpointNames.USER_ACTIVITIES)
    }

    @Test
    fun checkProfileCompletion_isCorrect() {
        assertEquals("check-profile-completion", AcmoEndpointNames.CHECK_PROFILE_COMPLETION)
    }

    @Test
    fun allEndpoints_areNotBlank() {
        val endpoints = listOf(
            AcmoEndpointNames.INITIALIZE,
            AcmoEndpointNames.OFFERS,
            AcmoEndpointNames.ACTIVE_OFFERS,
            AcmoEndpointNames.UPDATE_USER,
            AcmoEndpointNames.DEVICE_DETAILS,
            AcmoEndpointNames.OFFER_SUMMARY,
            AcmoEndpointNames.ENGAGEMENT,
            AcmoEndpointNames.USAGE_STATS,
            AcmoEndpointNames.USER_ACTIVITIES,
            AcmoEndpointNames.CHECK_PROFILE_COMPLETION
        )
        endpoints.forEach { endpoint ->
            assertTrue("Endpoint should not be blank: $endpoint", endpoint.isNotBlank())
        }
    }

    @Test
    fun allEndpoints_areUnique() {
        val endpoints = listOf(
            AcmoEndpointNames.INITIALIZE,
            AcmoEndpointNames.OFFERS,
            AcmoEndpointNames.ACTIVE_OFFERS,
            AcmoEndpointNames.UPDATE_USER,
            AcmoEndpointNames.DEVICE_DETAILS,
            AcmoEndpointNames.OFFER_SUMMARY,
            AcmoEndpointNames.ENGAGEMENT,
            AcmoEndpointNames.USAGE_STATS,
            AcmoEndpointNames.USER_ACTIVITIES,
            AcmoEndpointNames.CHECK_PROFILE_COMPLETION
        )
        assertEquals(
            "All endpoint names must be unique",
            endpoints.size,
            endpoints.toSet().size
        )
    }
}
