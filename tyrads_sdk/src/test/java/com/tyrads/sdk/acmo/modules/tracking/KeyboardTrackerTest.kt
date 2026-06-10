package com.tyrads.sdk.acmo.modules.tracking

import com.tyrads.sdk.acmo.core.utils.KeyboardTracker
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Unit tests for [KeyboardTracker] — score computation, capping, and validation.
 */
class KeyboardTrackerTest {

    @Before
    fun setUp() {
        KeyboardTracker.resetKeyboardCounters()
    }

    @After
    fun tearDown() {
        KeyboardTracker.resetKeyboardCounters()
    }

    // ---- Initial state ----

    @Test
    fun initialEventCount_isZero() {
        assertEquals(0, KeyboardTracker.getKeyboardEventCount())
    }

    @Test
    fun initialScore_isZero() {
        assertEquals(0, KeyboardTracker.getKeyboardScore())
    }

    // ---- Increment ----

    @Test
    fun incrementKeyboardEvent_incrementsByOne() {
        val result = KeyboardTracker.incrementKeyboardEvent()
        assertEquals(1, result)
        assertEquals(1, KeyboardTracker.getKeyboardEventCount())
    }

    // ---- Score calculation ----

    @Test
    fun calculateKeyboardScore_halfMax_returns50() {
        repeat(500) { KeyboardTracker.incrementKeyboardEvent() }
        val score = KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)
        assertEquals(50, score)
    }

    @Test
    fun calculateKeyboardScore_fullMax_returns100() {
        repeat(1000) { KeyboardTracker.incrementKeyboardEvent() }
        val score = KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)
        assertEquals(100, score)
    }

    @Test
    fun calculateKeyboardScore_exceedsMax_cappedAt100() {
        repeat(2000) { KeyboardTracker.incrementKeyboardEvent() }
        val score = KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)
        assertEquals(100, score)
    }

    @Test
    fun calculateKeyboardScore_zeroEvents_returnsZero() {
        val score = KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)
        assertEquals(0, score)
    }

    @Test
    fun calculateKeyboardScore_updatesStoredScore() {
        repeat(100) { KeyboardTracker.incrementKeyboardEvent() }
        KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)
        assertEquals(10, KeyboardTracker.getKeyboardScore())
    }

    // ---- setKeyboardScore validation ----

    @Test
    fun setKeyboardScore_negativeValue_clampedToZero() {
        KeyboardTracker.setKeyboardScore(-10)
        assertEquals(0, KeyboardTracker.getKeyboardScore())
    }

    @Test
    fun setKeyboardScore_exceedsMax_clampedTo100() {
        KeyboardTracker.setKeyboardScore(200)
        assertEquals(100, KeyboardTracker.getKeyboardScore())
    }

    @Test
    fun setKeyboardScore_validValue_setsCorrectly() {
        KeyboardTracker.setKeyboardScore(60)
        assertEquals(60, KeyboardTracker.getKeyboardScore())
    }

    @Test
    fun setKeyboardScore_boundary_zero() {
        KeyboardTracker.setKeyboardScore(0)
        assertEquals(0, KeyboardTracker.getKeyboardScore())
    }

    @Test
    fun setKeyboardScore_boundary_100() {
        KeyboardTracker.setKeyboardScore(100)
        assertEquals(100, KeyboardTracker.getKeyboardScore())
    }

    // ---- getAllKeyboardMetrics ----

    @Test
    fun getAllKeyboardMetrics_returnsCorrectMap() {
        repeat(200) { KeyboardTracker.incrementKeyboardEvent() }
        KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)

        val metrics = KeyboardTracker.getAllKeyboardMetrics()
        assertEquals(200, metrics["keyboard_num_events"])
        assertEquals(20, metrics["keyboard_score"])
    }

    // ---- Reset ----

    @Test
    fun resetKeyboardCounters_setsAllToZero() {
        repeat(10) { KeyboardTracker.incrementKeyboardEvent() }
        KeyboardTracker.setKeyboardScore(70)

        KeyboardTracker.resetKeyboardCounters()

        assertEquals(0, KeyboardTracker.getKeyboardEventCount())
        assertEquals(0, KeyboardTracker.getKeyboardScore())
    }

    // ---- Thread safety ----

    @Test
    fun concurrentIncrements_areThreadSafe() {
        val threadCount = 100
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(10)

        repeat(threadCount) {
            executor.submit {
                KeyboardTracker.incrementKeyboardEvent()
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        assertEquals(threadCount, KeyboardTracker.getKeyboardEventCount())
    }
}
