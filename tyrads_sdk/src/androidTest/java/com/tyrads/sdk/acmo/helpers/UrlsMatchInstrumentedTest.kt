package com.tyrads.sdk.acmo.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tyrads.sdk.acmo.helpers.urlsMatch
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [urlsMatch] using real Android Uri parsing.
 * These tests require a device/emulator where android.net.Uri works correctly.
 */
@RunWith(AndroidJUnit4::class)
class UrlsMatchInstrumentedTest {

    @Test
    fun sameHostAndSameTo_returnsTrue() {
        val url1 = "https://v4.sdk.tyrads.com/?to=offerwall&token=abc"
        val url2 = "https://v4.sdk.tyrads.com/?to=offerwall&token=xyz"
        assertTrue(urlsMatch(url1, url2))
    }

    @Test
    fun sameHostDifferentTo_returnsFalse() {
        val url1 = "https://v4.sdk.tyrads.com/?to=offerwall&token=abc"
        val url2 = "https://v4.sdk.tyrads.com/?to=campaigns&token=abc"
        assertFalse(urlsMatch(url1, url2))
    }

    @Test
    fun differentHost_returnsFalse() {
        val url1 = "https://v4.sdk.tyrads.com/?to=offerwall"
        val url2 = "https://v5.sdk.tyrads.com/?to=offerwall"
        assertFalse(urlsMatch(url1, url2))
    }

    @Test
    fun identicalUrls_returnsTrue() {
        val url = "https://v4.sdk.tyrads.com/?to=campaigns&token=abc"
        assertTrue(urlsMatch(url, url))
    }

    @Test
    fun sameToWithDifferentCampaignId_returnsFalse() {
        val url1 = "https://v4.sdk.tyrads.com/?to=campaigns/42&token=abc"
        val url2 = "https://v4.sdk.tyrads.com/?to=campaigns/99&token=abc"
        assertFalse(urlsMatch(url1, url2))
    }

    @Test
    fun sameToSameHost_differentOtherParams_returnsTrue() {
        val url1 = "https://v4.sdk.tyrads.com/?to=home&token=t1&lang=en"
        val url2 = "https://v4.sdk.tyrads.com/?to=home&token=t2&lang=ar"
        assertTrue(urlsMatch(url1, url2))
    }

    @Test
    fun malformedUrls_identicalStrings_returnsTrue() {
        assertTrue(urlsMatch("not-a-url", "not-a-url"))
    }

    @Test
    fun malformedUrls_differentStrings_returnsFalse() {
        assertFalse(urlsMatch("not-a-url", "different-string"))
    }

    @Test
    fun emptyStrings_returnsTrue() {
        assertTrue(urlsMatch("", ""))
    }
}
