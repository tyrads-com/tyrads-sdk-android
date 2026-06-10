package com.tyrads.sdk.acmo.core.utils

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Unit tests for [KeyboardTracker] singleton.
 * Validates atomic counter operations, score calculations, and thread-safety.
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

    @Test
    fun initialState_isZero() {
        assertEquals(0, KeyboardTracker.getKeyboardEventCount())
        assertEquals(0, KeyboardTracker.getKeyboardScore())
    }

    @Test
    fun incrementKeyboardEvent_incrementsCorrectly() {
        val result = KeyboardTracker.incrementKeyboardEvent()
        assertEquals(1, result)
        assertEquals(1, KeyboardTracker.getKeyboardEventCount())
    }

    @Test
    fun calculateKeyboardScore_calculatesCorrectly() {
        // Given 500 events out of max 1000
        repeat(500) { KeyboardTracker.incrementKeyboardEvent() }
        
        val score = KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)
        assertEquals(50, score) // 500/1000 = 50%
        assertEquals(50, KeyboardTracker.getKeyboardScore())
    }

    @Test
    fun calculateKeyboardScore_capsAt100() {
        // Given 1500 events out of max 1000
        repeat(1500) { KeyboardTracker.incrementKeyboardEvent() }
        
        val score = KeyboardTracker.calculateKeyboardScore(maxEvents = 1000)
        assertEquals(100, score) // Capped at 100
        assertEquals(100, KeyboardTracker.getKeyboardScore())
    }

    @Test
    fun setKeyboardScore_boundsCorrectly() {
        KeyboardTracker.setKeyboardScore(75)
        assertEquals(75, KeyboardTracker.getKeyboardScore())

        KeyboardTracker.setKeyboardScore(-5)
        assertEquals(0, KeyboardTracker.getKeyboardScore()) // Bounded lower

        KeyboardTracker.setKeyboardScore(200)
        assertEquals(100, KeyboardTracker.getKeyboardScore()) // Bounded upper
    }

    @Test
    fun getAllKeyboardMetrics_returnsCorrectMap() {
        repeat(200) { KeyboardTracker.incrementKeyboardEvent() }
        KeyboardTracker.calculateKeyboardScore(maxEvents = 1000) // Score = 20

        val metrics = KeyboardTracker.getAllKeyboardMetrics()
        assertEquals(200, metrics["keyboard_num_events"])
        assertEquals(20, metrics["keyboard_score"])
    }

    @Test
    fun concurrentIncrements_areThreadSafe() {
        val threadCount = 200
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
