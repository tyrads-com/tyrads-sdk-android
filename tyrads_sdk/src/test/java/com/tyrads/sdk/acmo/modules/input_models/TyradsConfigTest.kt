package com.tyrads.sdk.acmo.modules.input_models

import com.tyrads.sdk.acmo.modules.input_models.TyradsConfig
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [TyradsConfig] data class.
 */
class TyradsConfigTest {

    @Test
    fun defaultConfig_skipInitialPages_isFalse() {
        val config = TyradsConfig()
        assertFalse(config.skipInitialPages)
    }

    @Test
    fun customConfig_skipInitialPages_isTrue() {
        val config = TyradsConfig(skipInitialPages = true)
        assertTrue(config.skipInitialPages)
    }

    @Test
    fun twoDefaultConfigs_areEqual() {
        val config1 = TyradsConfig()
        val config2 = TyradsConfig()
        assertEquals(config1, config2)
    }

    @Test
    fun differentConfigs_areNotEqual() {
        val config1 = TyradsConfig(skipInitialPages = true)
        val config2 = TyradsConfig(skipInitialPages = false)
        assertNotEquals(config1, config2)
    }

    @Test
    fun config_copy_worksCorrectly() {
        val original = TyradsConfig(skipInitialPages = false)
        val copied = original.copy(skipInitialPages = true)
        assertTrue(copied.skipInitialPages)
        assertFalse(original.skipInitialPages)
    }

    @Test
    fun config_toString_containsFieldName() {
        val config = TyradsConfig(skipInitialPages = true)
        assertTrue(config.toString().contains("skipInitialPages"))
    }
}
