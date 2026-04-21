package com.tyrads.sdk.acmo.core.utils

import java.util.concurrent.atomic.AtomicInteger

object ClipboardTracker {
    private val clipboardEventCount = AtomicInteger(0)

    private val clipboardScore = AtomicInteger(0)

    fun incrementClipboardEvent(): Int {
        return clipboardEventCount.incrementAndGet()
    }

    fun getClipboardEventCount(): Int {
        return clipboardEventCount.get()
    }

    fun calculateClipboardScore(maxEvents: Int = 500): Int {
        val eventCount = clipboardEventCount.get()
        val score = ((eventCount.toDouble() / maxEvents.toDouble()) * 100).toInt()
        val cappedScore = if (score > 100) 100 else score
        clipboardScore.set(cappedScore)
        return cappedScore
    }

    fun getClipboardScore(): Int {
        return clipboardScore.get()
    }

    fun setClipboardScore(score: Int) {
        val validScore = when {
            score < 0 -> 0
            score > 100 -> 100
            else -> score
        }
        clipboardScore.set(validScore)
    }

    @Suppress("UNUSED")
    fun resetClipboardCounters() {
        clipboardEventCount.set(0)
        clipboardScore.set(0)
    }

    @Suppress("UNUSED")
    fun getAllClipboardMetrics(): Map<String, Int> {
        return mapOf(
            "clipboard_num_events" to getClipboardEventCount(),
            "clipboard_score" to getClipboardScore()
        )
    }
}
