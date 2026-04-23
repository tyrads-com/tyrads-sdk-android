package com.tyrads.sdk.acmo.modules.tracking

import com.tyrads.sdk.acmo.core.utils.ClipboardTracker
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Unit tests for [ClipboardTracker] — score computation, capping, and validation.
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

    // ---- Initial state ----

    @Test
    fun initialEventCount_isZero() {
        assertEquals(0, ClipboardTracker.getClipboardEventCount())
    }

    @Test
    fun initialScore_isZero() {
        assertEquals(0, ClipboardTracker.getClipboardScore())
    }

    // ---- Increment ----

    @Test
    fun incrementClipboardEvent_incrementsByOne() {
        val result = ClipboardTracker.incrementClipboardEvent()
        assertEquals(1, result)
        assertEquals(1, ClipboardTracker.getClipboardEventCount())
    }

    // ---- Score calculation ----

    @Test
    fun calculateClipboardScore_halfMax_returns50() {
        repeat(50) { ClipboardTracker.incrementClipboardEvent() }
        val score = ClipboardTracker.calculateClipboardScore(maxEvents = 100)
        assertEquals(50, score)
    }

    @Test
    fun calculateClipboardScore_fullMax_returns100() {
        repeat(500) { ClipboardTracker.incrementClipboardEvent() }
        val score = ClipboardTracker.calculateClipboardScore(maxEvents = 500)
        assertEquals(100, score)
    }

    @Test
    fun calculateClipboardScore_exceedsMax_cappedAt100() {
        repeat(1000) { ClipboardTracker.incrementClipboardEvent() }
        val score = ClipboardTracker.calculateClipboardScore(maxEvents = 500)
        assertEquals(100, score)
    }

    @Test
    fun calculateClipboardScore_zeroEvents_returnsZero() {
        val score = ClipboardTracker.calculateClipboardScore(maxEvents = 500)
        assertEquals(0, score)
    }

    @Test
    fun calculateClipboardScore_updatesStoredScore() {
        repeat(250) { ClipboardTracker.incrementClipboardEvent() }
        ClipboardTracker.calculateClipboardScore(maxEvents = 500)
        assertEquals(50, ClipboardTracker.getClipboardScore())
    }

    // ---- setClipboardScore validation ----

    @Test
    fun setClipboardScore_negativeValue_clampedToZero() {
        ClipboardTracker.setClipboardScore(-5)
        assertEquals(0, ClipboardTracker.getClipboardScore())
    }

    @Test
    fun setClipboardScore_exceedsMax_clampedTo100() {
        ClipboardTracker.setClipboardScore(150)
        assertEquals(100, ClipboardTracker.getClipboardScore())
    }

    @Test
    fun setClipboardScore_validValue_setsCorrectly() {
        ClipboardTracker.setClipboardScore(75)
        assertEquals(75, ClipboardTracker.getClipboardScore())
    }

    @Test
    fun setClipboardScore_zero_setsZero() {
        ClipboardTracker.setClipboardScore(0)
        assertEquals(0, ClipboardTracker.getClipboardScore())
    }

    @Test
    fun setClipboardScore_100_sets100() {
        ClipboardTracker.setClipboardScore(100)
        assertEquals(100, ClipboardTracker.getClipboardScore())
    }

    // ---- getAllClipboardMetrics ----

    @Test
    fun getAllClipboardMetrics_returnsCorrectMap() {
        repeat(25) { ClipboardTracker.incrementClipboardEvent() }
        ClipboardTracker.calculateClipboardScore(maxEvents = 100)

        val metrics = ClipboardTracker.getAllClipboardMetrics()
        assertEquals(25, metrics["clipboard_num_events"])
        assertEquals(25, metrics["clipboard_score"])
    }

    // ---- Reset ----

    @Test
    fun resetClipboardCounters_setsAllToZero() {
        repeat(10) { ClipboardTracker.incrementClipboardEvent() }
        ClipboardTracker.setClipboardScore(80)

        ClipboardTracker.resetClipboardCounters()

        assertEquals(0, ClipboardTracker.getClipboardEventCount())
        assertEquals(0, ClipboardTracker.getClipboardScore())
    }

    // ---- Thread safety ----

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
