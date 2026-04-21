package com.tyrads.sdk.acmo.core.utils

import java.util.concurrent.atomic.AtomicInteger

object KeyboardTracker {
    private val keyboardEventCount = AtomicInteger(0)

    private val keyboardScore = AtomicInteger(0)

    fun incrementKeyboardEvent(): Int {
        return keyboardEventCount.incrementAndGet()
    }

    fun getKeyboardEventCount(): Int {
        return keyboardEventCount.get()
    }

    fun calculateKeyboardScore(maxEvents: Int = 1000): Int {
        val eventCount = keyboardEventCount.get()
        val score = ((eventCount.toDouble() / maxEvents.toDouble()) * 100).toInt()
        val cappedScore = if (score > 100) 100 else score
        keyboardScore.set(cappedScore)
        return cappedScore
    }

    fun getKeyboardScore(): Int {
        return keyboardScore.get()
    }

    fun setKeyboardScore(score: Int) {
        val validScore = when {
            score < 0 -> 0
            score > 100 -> 100
            else -> score
        }
        keyboardScore.set(validScore)
    }

    @Suppress("UNUSED")
    fun resetKeyboardCounters() {
        keyboardEventCount.set(0)
        keyboardScore.set(0)
    }

    @Suppress("UNUSED")
    fun getAllKeyboardMetrics(): Map<String, Int> {
        return mapOf(
            "keyboard_num_events" to getKeyboardEventCount(),
            "keyboard_score" to getKeyboardScore()
        )
    }
}