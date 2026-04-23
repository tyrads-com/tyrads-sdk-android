package com.tyrads.sdk.acmo.helpers

import com.tyrads.sdk.acmo.helpers.urlsMatch
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the [urlsMatch] utility function.
 *
 * IMPORTANT CONTEXT: urlsMatch uses `android.net.Uri` (via androidx.core.net.toUri()).
 * In JVM unit tests, android.net.Uri methods throw "Method not mocked" exceptions,
 * causing the urlsMatch function to fall back to the catch block `url1 == url2`.
 * 
 * Tests in this file are calibrated to the ACTUAL JVM behavior (string equality).
 * Android-specific behavior (real Uri parsing) is tested in androidTest.
 */
class UrlsMatchTest {

    @Test
    fun identicalUrls_returnsTrue() {
        val url = "https://v4.sdk.tyrads.com/?to=campaigns&token=abc&lang=en"
        assertTrue("Identical URLs must match", urlsMatch(url, url))
    }

    @Test
    fun differentUrls_returnsFalse_onJvm() {
        val url1 = "https://v4.sdk.tyrads.com/?to=offerwall&token=abc"
        val url2 = "https://v4.sdk.tyrads.com/?to=campaigns&token=abc"
        // On JVM, Uri parsing throws, so it falls back to url1 == url2 -> false
        assertFalse("Different URLs fail string equality fallback", urlsMatch(url1, url2))
    }

    @Test
    fun malformedUrls_identicalStrings_returnsTrue() {
        val url = "not-a-valid-url"
        assertTrue("Identical malformed URLs fall back to string equality -> true", urlsMatch(url, url))
    }

    @Test
    fun malformedUrls_differentStrings_returnsFalse() {
        assertFalse("Different malformed strings should not match", urlsMatch("not-a-url", "different-string"))
    }

    @Test
    fun emptyStrings_returnsTrue() {
        assertTrue("Two empty strings are equal", urlsMatch("", ""))
    }

    @Test
    fun emptyVsNonEmpty_returnsFalse() {
        assertFalse("Empty vs non-empty string equality -> false", urlsMatch("", "https://example.com"))
    }
}
