package com.tyrads.sdk.acmo.modules.input_models

import com.tyrads.sdk.TyradsMediaSourceInfo
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [TyradsMediaSourceInfo] data class.
 * Verifies default null values, field setting, equality, and copy semantics.
 */
class TyradsMediaSourceInfoTest {

    // ---- Default values ----

    @Test
    fun defaultMediaSourceInfo_allFieldsAreNull() {
        val info = TyradsMediaSourceInfo()
        assertNull(info.mediaSourceName)
        assertNull(info.mediaCampaignName)
        assertNull(info.mediaSourceId)
        assertNull(info.mediaSubSourceId)
        assertNull(info.incentivized)
        assertNull(info.mediaAdsetName)
        assertNull(info.mediaAdsetId)
        assertNull(info.mediaCreativeName)
        assertNull(info.mediaCreativeId)
        assertNull(info.sub1)
        assertNull(info.sub2)
        assertNull(info.sub3)
        assertNull(info.sub4)
        assertNull(info.sub5)
    }

    // ---- Individual field setting ----

    @Test
    fun mediaSourceName_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaSourceName = "Facebook Ads")
        assertEquals("Facebook Ads", info.mediaSourceName)
    }

    @Test
    fun mediaCampaignName_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaCampaignName = "Summer Campaign")
        assertEquals("Summer Campaign", info.mediaCampaignName)
    }

    @Test
    fun mediaSourceId_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaSourceId = "src_001")
        assertEquals("src_001", info.mediaSourceId)
    }

    @Test
    fun mediaSubSourceId_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaSubSourceId = "sub_002")
        assertEquals("sub_002", info.mediaSubSourceId)
    }

    @Test
    fun incentivized_true_setsCorrectly() {
        val info = TyradsMediaSourceInfo(incentivized = true)
        assertTrue(info.incentivized == true)
    }

    @Test
    fun incentivized_false_setsCorrectly() {
        val info = TyradsMediaSourceInfo(incentivized = false)
        assertFalse(info.incentivized == true)
    }

    @Test
    fun mediaAdsetName_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaAdsetName = "Ad Set A")
        assertEquals("Ad Set A", info.mediaAdsetName)
    }

    @Test
    fun mediaAdsetId_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaAdsetId = "adset_123")
        assertEquals("adset_123", info.mediaAdsetId)
    }

    @Test
    fun mediaCreativeName_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaCreativeName = "Banner 300x250")
        assertEquals("Banner 300x250", info.mediaCreativeName)
    }

    @Test
    fun mediaCreativeId_setsCorrectly() {
        val info = TyradsMediaSourceInfo(mediaCreativeId = "cr_abc")
        assertEquals("cr_abc", info.mediaCreativeId)
    }

    @Test
    fun subFields_setCorrectly() {
        val info = TyradsMediaSourceInfo(
            sub1 = "s1", sub2 = "s2", sub3 = "s3", sub4 = "s4", sub5 = "s5"
        )
        assertEquals("s1", info.sub1)
        assertEquals("s2", info.sub2)
        assertEquals("s3", info.sub3)
        assertEquals("s4", info.sub4)
        assertEquals("s5", info.sub5)
    }

    // ---- Full object ----

    @Test
    fun fullMediaSourceInfo_allFieldsSet() {
        val info = TyradsMediaSourceInfo(
            mediaSourceName = "Google Ads",
            mediaCampaignName = "Q4 Push",
            mediaSourceId = "google_001",
            mediaSubSourceId = "sub_google",
            incentivized = true,
            mediaAdsetName = "Retargeting Set",
            mediaAdsetId = "adset_999",
            mediaCreativeName = "Video 15s",
            mediaCreativeId = "cr_v15",
            sub1 = "custom1",
            sub2 = "custom2",
            sub3 = "custom3",
            sub4 = "custom4",
            sub5 = "custom5"
        )
        assertEquals("Google Ads", info.mediaSourceName)
        assertEquals("Q4 Push", info.mediaCampaignName)
        assertEquals("google_001", info.mediaSourceId)
        assertEquals("sub_google", info.mediaSubSourceId)
        assertTrue(info.incentivized == true)
        assertEquals("Retargeting Set", info.mediaAdsetName)
        assertEquals("adset_999", info.mediaAdsetId)
        assertEquals("Video 15s", info.mediaCreativeName)
        assertEquals("cr_v15", info.mediaCreativeId)
        assertEquals("custom1", info.sub1)
        assertEquals("custom2", info.sub2)
        assertEquals("custom3", info.sub3)
        assertEquals("custom4", info.sub4)
        assertEquals("custom5", info.sub5)
    }

    // ---- Equality ----

    @Test
    fun twoIdenticalMediaSourceInfos_areEqual() {
        val info1 = TyradsMediaSourceInfo(mediaSourceName = "Meta", incentivized = false)
        val info2 = TyradsMediaSourceInfo(mediaSourceName = "Meta", incentivized = false)
        assertEquals(info1, info2)
    }

    @Test
    fun differentMediaSourceInfos_areNotEqual() {
        val info1 = TyradsMediaSourceInfo(mediaSourceName = "Meta")
        val info2 = TyradsMediaSourceInfo(mediaSourceName = "TikTok")
        assertNotEquals(info1, info2)
    }

    // ---- Copy semantics ----

    @Test
    fun copy_changesOnlySpecifiedField() {
        val original = TyradsMediaSourceInfo(mediaSourceName = "Original", incentivized = false)
        val copied = original.copy(mediaSourceName = "Copied")
        assertEquals("Copied", copied.mediaSourceName)
        assertFalse(copied.incentivized == true)
        assertEquals("Original", original.mediaSourceName) // original unchanged
    }

    @Test
    fun copy_defaultArgs_producesIdenticalObject() {
        val original = TyradsMediaSourceInfo(sub3 = "track_me")
        val copy = original.copy()
        assertEquals(original, copy)
        assertNotSame(original, copy)
    }
}
