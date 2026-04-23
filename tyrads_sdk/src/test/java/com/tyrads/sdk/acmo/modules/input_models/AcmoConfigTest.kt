package com.tyrads.sdk.acmo.modules.input_models

import AcmoConfig
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [AcmoConfig] constants.
 * Ensures all SDK configuration values remain as expected.
 */
class AcmoConfigTest {

    @Test
    fun apiVersion_isCorrect() {
        assertEquals("4.0", AcmoConfig.API_VERSION)
    }

    @Test
    fun buildVersion_isCorrect() {
        assertEquals("1", AcmoConfig.BUILD_VERSION)
    }

    @Test
    fun av_isCorrect() {
        assertEquals("4", AcmoConfig.AV)
    }

    @Test
    fun sdkVersion_containsBuildVersion() {
        assertTrue(
            "SDK_VERSION should contain BUILD_VERSION",
            AcmoConfig.SDK_VERSION.contains(AcmoConfig.BUILD_VERSION)
        )
    }

    @Test
    fun sdkVersion_containsApiVersion() {
        assertTrue(
            "SDK_VERSION should contain API_VERSION",
            AcmoConfig.SDK_VERSION.contains(AcmoConfig.API_VERSION)
        )
    }

    @Test
    fun sdkVersion_matchesExpectedFormat() {
        assertEquals("4.0.0-1", AcmoConfig.SDK_VERSION)
    }

    @Test
    fun sdkPlatform_isAndroid() {
        assertEquals("Android", AcmoConfig.SDK_PLATFORM)
    }

    @Test
    fun baseUrl_startsWithHttps() {
        assertTrue(
            "BASE_URL should start with https://",
            AcmoConfig.BASE_URL.startsWith("https://")
        )
    }

    @Test
    fun baseUrl_containsApiVersion() {
        assertTrue(
            "BASE_URL should contain API_VERSION",
            AcmoConfig.BASE_URL.contains(AcmoConfig.API_VERSION)
        )
    }

    @Test
    fun baseUrl_endsWithSlash() {
        assertTrue(
            "BASE_URL should end with '/'",
            AcmoConfig.BASE_URL.endsWith("/")
        )
    }

    @Test
    fun baseUrl_matchesExpectedValue() {
        assertEquals("https://api.tyrads.com/v4.0/", AcmoConfig.BASE_URL)
    }

    @Test
    fun tag_isNotBlank() {
        assertTrue("TAG must not be blank", AcmoConfig.TAG.isNotBlank())
    }

    @Test
    fun tag_isCorrect() {
        assertEquals("TyrAds SDK", AcmoConfig.TAG)
    }
}
