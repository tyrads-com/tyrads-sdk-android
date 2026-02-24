package com.tyrads.sdk.acmo.core.utils

import androidx.annotation.Keep

@Keep
object SecurityUtils {
    private const val SALT = "3252dce0de296b87"
    fun deobfuscate(bytes: ByteArray): String {
        val result = ByteArray(bytes.size)
        val saltBytes = SALT.toByteArray()
        for (i in bytes.indices) {
            result[i] = (bytes[i].toInt() xor saltBytes[i % saltBytes.size].toInt()).toByte()
        }
        return String(result)
    }
}