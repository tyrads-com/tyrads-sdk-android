package com.tyrads.sdk.acmo.modules.input_models

import com.tyrads.sdk.TyradsUserInfo
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [TyradsUserInfo] data class.
 */
class TyradsUserInfoTest {

    @Test
    fun defaultUserInfo_allFieldsAreNull() {
        val userInfo = TyradsUserInfo()
        assertNull(userInfo.email)
        assertNull(userInfo.phoneNumber)
        assertNull(userInfo.userGroup)
        assertNull(userInfo.age)
        assertNull(userInfo.gender)
    }

    @Test
    fun userInfoWithEmail_setsEmailCorrectly() {
        val userInfo = TyradsUserInfo(email = "test@example.com")
        assertEquals("test@example.com", userInfo.email)
        assertNull(userInfo.phoneNumber)
        assertNull(userInfo.userGroup)
    }

    @Test
    fun userInfoWithPhone_setsPhoneCorrectly() {
        val userInfo = TyradsUserInfo(phoneNumber = "001555123456")
        assertEquals("001555123456", userInfo.phoneNumber)
        assertNull(userInfo.email)
    }

    @Test
    fun userInfoWithGroup_setsGroupCorrectly() {
        val userInfo = TyradsUserInfo(userGroup = "premium")
        assertEquals("premium", userInfo.userGroup)
    }

    @Test
    fun userInfoWithAge_setsAgeCorrectly() {
        val userInfo = TyradsUserInfo(age = 25)
        assertEquals(25, userInfo.age)
    }

    @Test
    fun userInfoWithGender_setsGenderCorrectly() {
        val userInfo = TyradsUserInfo(gender = 1)
        assertEquals(1, userInfo.gender)
    }

    @Test
    fun fullUserInfo_allFieldsSet() {
        val userInfo = TyradsUserInfo(
            email = "user@test.com",
            phoneNumber = "001555987654",
            userGroup = "vip",
            age = 30,
            gender = 2
        )
        assertEquals("user@test.com", userInfo.email)
        assertEquals("001555987654", userInfo.phoneNumber)
        assertEquals("vip", userInfo.userGroup)
        assertEquals(30, userInfo.age)
        assertEquals(2, userInfo.gender)
    }

    @Test
    fun twoIdenticalUserInfos_areEqual() {
        val info1 = TyradsUserInfo(email = "a@b.com", phoneNumber = "123", userGroup = "g1", age = 20, gender = 1)
        val info2 = TyradsUserInfo(email = "a@b.com", phoneNumber = "123", userGroup = "g1", age = 20, gender = 1)
        assertEquals(info1, info2)
    }

    @Test
    fun differentUserInfos_areNotEqual() {
        val info1 = TyradsUserInfo(email = "a@b.com")
        val info2 = TyradsUserInfo(email = "c@d.com")
        assertNotEquals(info1, info2)
    }

    @Test
    fun userInfo_copy_worksCorrectly() {
        val original = TyradsUserInfo(email = "original@test.com")
        val copied = original.copy(email = "copy@test.com")
        assertEquals("copy@test.com", copied.email)
        assertEquals("original@test.com", original.email)
    }
}
