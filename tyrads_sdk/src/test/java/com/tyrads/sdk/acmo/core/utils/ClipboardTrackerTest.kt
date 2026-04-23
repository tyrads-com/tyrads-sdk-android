package com.tyrads.sdk.acmo.core.utils

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Unit tests for [ClipboardTracker] singleton.
 * Validates atomic counter operations, score calculations, and thread-safety.
 */
class ClipboardTrackerTest {

    @Before
    fun setUp() {
        ClipboardTracker.resetClipboardCounters()
    }

    @After
    fun tearDown() {
        ClipboardTracker.resetClipboardCounters()
    }

    @Test
    fun initialState_isZero() {
        assertEquals(0, ClipboardTracker.getClipboardEventCount())
        assertEquals(0, ClipboardTracker.getClipboardScore())
    }

    @Test
    fun incrementClipboardEvent_incrementsCorrectly() {
        val result = ClipboardTracker.incrementClipboardEvent()
        assertEquals(1, result)
        assertEquals(1, ClipboardTracker.getClipboardEventCount())
    }

    @Test
    fun calculateClipboardScore_calculatesCorrectly() {
        // Given 250 events out of max 500
        repeat(250) { ClipboardTracker.incrementClipboardEvent() }
        
        val score = ClipboardTracker.calculateClipboardScore(maxEvents = 500)
        assertEquals(50, score) // 250/500 = 50%
        assertEquals(50, ClipboardTracker.getClipboardScore())
    }

    @Test
    fun calculateClipboardScore_capsAt100() {
        // Given 600 events out of max 500
        repeat(600) { ClipboardTracker.incrementClipboardEvent() }
        
        val score = ClipboardTracker.calculateClipboardScore(maxEvents = 500)
        assertEquals(100, score) // Capped at 100
        assertEquals(100, ClipboardTracker.getClipboardScore())
    }

    @Test
    fun setClipboardScore_boundsCorrectly() {
        ClipboardTracker.setClipboardScore(50)
        assertEquals(50, ClipboardTracker.getClipboardScore())

        ClipboardTracker.setClipboardScore(-10)
        assertEquals(0, ClipboardTracker.getClipboardScore()) // Bounded lower

        ClipboardTracker.setClipboardScore(150)
        assertEquals(100, ClipboardTracker.getClipboardScore()) // Bounded upper
    }

    @Test
    fun getAllClipboardMetrics_returnsCorrectMap() {
        repeat(100) { ClipboardTracker.incrementClipboardEvent() }
        ClipboardTracker.calculateClipboardScore(maxEvents = 500) // Score = 20

        val metrics = ClipboardTracker.getAllClipboardMetrics()
        assertEquals(100, metrics["clipboard_num_events"])
        assertEquals(20, metrics["clipboard_score"])
    }

    @Test
    fun concurrentIncrements_areThreadSafe() {
        val threadCount = 100
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(10)

        repeat(threadCount) {
            executor.submit {
                ClipboardTracker.incrementClipboardEvent()
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        assertEquals(threadCount, ClipboardTracker.getClipboardEventCount())
    }
}
