package com.tyrads.sdk.acmo.core.extensions

import com.tyrads.sdk.acmo.core.extensions.formatTimeRemaining
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.core.extensions.removeTrailingZeros
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for numeral extension functions:
 * - [Double.numeral]
 * - [String.removeTrailingZeros]
 * - [Int?.formatTimeRemaining]
 */
class NumeralExtensionTest {

    // ============================================================
    // numeral() — threshold and suffix tests
    // ============================================================

    @Test
    fun numeral_below1000_returnsRawString() {
        assertEquals("999.0", 999.0.numeral())
    }

    @Test
    fun numeral_exactly1000_returnsK() {
        assertEquals("1K", 1_000.0.numeral())
    }

    @Test
    fun numeral_1500_returns1Point5K() {
        assertEquals("1.50K", 1_500.0.numeral())
    }

    @Test
    fun numeral_1250_returns1Point25K() {
        assertEquals("1.25K", 1_250.0.numeral())
    }

    @Test
    fun numeral_exactly1Million_returnsM() {
        assertEquals("1M", 1_000_000.0.numeral())
    }

    @Test
    fun numeral_1Point5Million_returns1Point5M() {
        assertEquals("1.50M", 1_500_000.0.numeral())
    }

    @Test
    fun numeral_exactly1Billion_returnsB() {
        assertEquals("1B", 1_000_000_000.0.numeral())
    }

    @Test
    fun numeral_2Point5Billion_returns2Point5B() {
        assertEquals("2.50B", 2_500_000_000.0.numeral())
    }

    @Test
    fun numeral_exactly1Trillion_returnsT() {
        assertEquals("1T", 1_000_000_000_000.0.numeral())
    }

    @Test
    fun numeral_trailingZerosAreStripped() {
        // 2000 -> "2.00K" -> stripped to "2K"
        assertEquals("2K", 2_000.0.numeral())
    }

    @Test
    fun numeral_zero_returnsZeroString() {
        assertEquals("0.0", 0.0.numeral())
    }

    @Test
    fun numeral_negative_returnsRawString() {
        // Negative numbers fall through to else branch
        val result = (-500.0).numeral()
        assertEquals("-500.0", result)
    }

    // ============================================================
    // removeTrailingZeros()
    // ============================================================

    @Test
    fun removeTrailingZeros_noTrailingZeros_unchanged() {
        assertEquals("1.25K", "1.25K".removeTrailingZeros())
    }

    @Test
    fun removeTrailingZeros_singleTrailingZero_removed() {
        assertEquals("1.50K", "1.50K".removeTrailingZeros())
    }

    @Test
    fun removeTrailingZeros_allDecimalsZero_dotRemoved() {
        assertEquals("2K", "2.00K".removeTrailingZeros())
    }

    @Test
    fun removeTrailingZeros_noDecimalPart_unchanged() {
        assertEquals("100", "100".removeTrailingZeros())
    }

    @Test
    fun removeTrailingZeros_emptyString_unchanged() {
        assertEquals("", "".removeTrailingZeros())
    }

    // ============================================================
    // formatTimeRemaining()
    // ============================================================

    @Test
    fun formatTimeRemaining_null_returnsNull() {
        assertNull(null.formatTimeRemaining())
    }

    @Test
    fun formatTimeRemaining_zero_returnsNull() {
        assertNull(0.formatTimeRemaining())
    }

    @Test
    fun formatTimeRemaining_negative_returnsNull() {
        assertNull((-1).formatTimeRemaining())
    }

    @Test
    fun formatTimeRemaining_lessThan1Day_returnsHMS() {
        // 3661 seconds = 1 hour, 1 minute, 1 second
        val result = 3661.formatTimeRemaining()
        assertEquals("01:01:01", result)
    }

    @Test
    fun formatTimeRemaining_exactly1Hour_returns01_00_00() {
        val result = 3600.formatTimeRemaining()
        assertEquals("01:00:00", result)
    }

    @Test
    fun formatTimeRemaining_exactly1Minute_returns00_01_00() {
        val result = 60.formatTimeRemaining()
        assertEquals("00:01:00", result)
    }

    @Test
    fun formatTimeRemaining_1Second_returns00_00_01() {
        val result = 1.formatTimeRemaining()
        assertEquals("00:00:01", result)
    }

    @Test
    fun formatTimeRemaining_moreThan1Day_returnsDayHourFormat() {
        // 86400 + 3600 = 1 day, 1 hour
        val result = (86400 + 3600).formatTimeRemaining()
        assertEquals("01d 01h", result)
    }

    @Test
    fun formatTimeRemaining_exactly1Day_returns01d_00h() {
        val result = 86400.formatTimeRemaining()
        assertEquals("01d 00h", result)
    }

    @Test
    fun formatTimeRemaining_2Days5Hours_returns02d_05h() {
        // 2 * 86400 + 5 * 3600 = 190800
        val result = (2 * 86400 + 5 * 3600).formatTimeRemaining()
        assertEquals("02d 05h", result)
    }

    @Test
    fun formatTimeRemaining_59Seconds_returns00_00_59() {
        val result = 59.formatTimeRemaining()
        assertEquals("00:00:59", result)
    }

    @Test
    fun formatTimeRemaining_padsSingleDigits() {
        // 9 seconds
        val result = 9.formatTimeRemaining()
        assertEquals("00:00:09", result)
    }
}
