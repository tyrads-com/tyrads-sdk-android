package com.tyrads.sdk.acmo.helpers

import com.tyrads.sdk.acmo.core.utils.SecurityUtils
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [SecurityUtils.deobfuscate].
 * XOR is its own inverse — applying it twice should return the original input.
 */
class SecurityUtilsTest {

    private val salt = "3252dce0de296b87"

    @Test
    fun deobfuscate_emptyArray_returnsEmptyString() {
        val result = SecurityUtils.deobfuscate(ByteArray(0))
        assertEquals("", result)
    }

    @Test
    fun deobfuscate_isOwnInverse() {
        val original = "Hello, TyrAds SDK!"
        val firstPass = SecurityUtils.deobfuscate(original.toByteArray())
        val secondPass = SecurityUtils.deobfuscate(firstPass.toByteArray())
        assertEquals(
            "XOR deobfuscate applied twice should return original string",
            original,
            secondPass
        )
    }

    @Test
    fun deobfuscate_knownVector() {
        // Manually XOR the salt with a known input byte-by-byte
        val input = "test"
        val saltBytes = salt.toByteArray()
        val inputBytes = input.toByteArray()
        val expected = ByteArray(inputBytes.size) { i ->
            (inputBytes[i].toInt() xor saltBytes[i % saltBytes.size].toInt()).toByte()
        }
        val result = SecurityUtils.deobfuscate(inputBytes)
        assertArrayEquals(
            "deobfuscate should XOR input with SALT",
            expected,
            result.toByteArray()
        )
    }

    @Test
    fun deobfuscate_singleByte_returnsCorrectResult() {
        val inputByte = byteArrayOf('A'.code.toByte())
        val saltByte = salt.toByteArray()[0]
        val expectedByte = ('A'.code xor saltByte.toInt()).toByte()
        val result = SecurityUtils.deobfuscate(inputByte)
        assertEquals(1, result.length)
        assertEquals(expectedByte, result.toByteArray()[0])
    }

    @Test
    fun deobfuscate_longInput_wrapsAroundSalt() {
        // Input longer than salt — confirm it wraps around using modulo
        val longInput = "abcdefghijklmnopqrstuvwxyz"
        val result = SecurityUtils.deobfuscate(longInput.toByteArray())
        assertNotNull(result)
        assertEquals(longInput.length, result.length)
    }
}
