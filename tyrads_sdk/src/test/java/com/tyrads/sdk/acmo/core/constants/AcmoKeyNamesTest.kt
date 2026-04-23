package com.tyrads.sdk.acmo.core.constants

import AcmoKeyNames
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [AcmoKeyNames] SharedPreferences key constants.
 */
class AcmoKeyNamesTest {

    private val prefix = AcmoKeyNames.PREFIX

    @Test
    fun prefix_isCorrect() {
        assertEquals("acmo_tyrads_sdk_", prefix)
    }

    @Test
    fun introductionComplete_hasPrefix() {
        assertTrue(AcmoKeyNames.INTRODUCTION_COMPLETE.startsWith(prefix))
    }

    @Test
    fun loggedIn_hasPrefix() {
        assertTrue(AcmoKeyNames.LOGGED_IN.startsWith(prefix))
    }

    @Test
    fun userId_hasPrefix() {
        assertTrue(AcmoKeyNames.USER_ID.startsWith(prefix))
    }

    @Test
    fun apiKey_hasPrefix() {
        assertTrue(AcmoKeyNames.API_KEY.startsWith(prefix))
    }

    @Test
    fun apiSecret_hasPrefix() {
        assertTrue(AcmoKeyNames.API_SECRET.startsWith(prefix))
    }

    @Test
    fun username_hasPrefix() {
        assertTrue(AcmoKeyNames.USERNAME.startsWith(prefix))
    }

    @Test
    fun fcmToken_hasPrefix() {
        assertTrue(AcmoKeyNames.FCM_TOKEN.startsWith(prefix))
    }

    @Test
    fun userData_hasPrefix() {
        assertTrue(AcmoKeyNames.USER_DATA.startsWith(prefix))
    }

    @Test
    fun advertisingId_hasPrefix() {
        assertTrue(AcmoKeyNames.ADVERTISING_ID.startsWith(prefix))
    }

    @Test
    fun trackedCampaigns_hasPrefix() {
        assertTrue(AcmoKeyNames.TRACKED_CAMPAIGNS_FOR_USER_ID.startsWith(prefix))
    }

    @Test
    fun playPerMinutePackages_hasPrefix() {
        assertTrue(AcmoKeyNames.PLAY_PER_MINUTE_PACKAGES.startsWith(prefix))
    }

    @Test
    fun privacyAccepted_hasPrefix() {
        assertTrue(AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID.startsWith(prefix))
    }

    @Test
    fun language_hasPrefix() {
        assertTrue(AcmoKeyNames.LANGUAGE.startsWith(prefix))
    }

    @Test
    fun playIntegrityToken_hasPrefix() {
        assertTrue(AcmoKeyNames.PLAY_INTEGRITY_TOKEN.startsWith(prefix))
    }

    @Test
    fun skipUserInfo_hasPrefix() {
        assertTrue(AcmoKeyNames.SKIP_USER_INFO.startsWith(prefix))
    }

    @Test
    fun allFixedKeys_areUnique() {
        val keys = listOf(
            AcmoKeyNames.INTRODUCTION_COMPLETE,
            AcmoKeyNames.LOGGED_IN,
            AcmoKeyNames.USER_ID,
            AcmoKeyNames.API_KEY,
            AcmoKeyNames.API_SECRET,
            AcmoKeyNames.USERNAME,
            AcmoKeyNames.FCM_TOKEN,
            AcmoKeyNames.USER_DATA,
            AcmoKeyNames.ADVERTISING_ID,
            AcmoKeyNames.PLAY_PER_MINUTE_PACKAGES,
            AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID,
            AcmoKeyNames.LANGUAGE,
            AcmoKeyNames.PLAY_INTEGRITY_TOKEN,
            AcmoKeyNames.SKIP_USER_INFO
        )
        assertEquals(
            "All SharedPreferences keys must be unique",
            keys.size,
            keys.toSet().size
        )
    }
}
