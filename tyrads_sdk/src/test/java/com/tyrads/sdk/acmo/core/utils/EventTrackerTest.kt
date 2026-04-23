package com.tyrads.sdk.acmo.core.utils

import com.tyrads.sdk.acmo.core.utils.EventTracker
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Unit tests for [EventTracker] singleton.
 * Validates atomic counter operations and thread-safety.
 */
class EventTrackerTest {

    @Before
    fun setUp() {
        EventTracker.resetAllCounters()
    }

    @After
    fun tearDown() {
        EventTracker.resetAllCounters()
    }

    // ---- Initial state ----

    @Test
    fun initialTouchCount_isZero() {
        assertEquals(0, EventTracker.getTouchEventCount())
    }

    @Test
    fun initialClickCount_isZero() {
        assertEquals(0, EventTracker.getClickEventCount())
    }

    @Test
    fun initialMouseCount_isZero() {
        assertEquals(0, EventTracker.getMouseEventCount())
    }

    @Test
    fun initialClipboardCount_isZero() {
        assertEquals(0, EventTracker.getClipboardEventCount())
    }

    // ---- Increment tests ----

    @Test
    fun incrementTouchEvent_incrementsByOne() {
        val result = EventTracker.incrementTouchEvent()
        assertEquals(1, result)
        assertEquals(1, EventTracker.getTouchEventCount())
    }

    @Test
    fun incrementClickEvent_incrementsByOne() {
        val result = EventTracker.incrementClickEvent()
        assertEquals(1, result)
        assertEquals(1, EventTracker.getClickEventCount())
    }

    @Test
    fun incrementMouseEvent_incrementsByOne() {
        val result = EventTracker.incrementMouseEvent()
        assertEquals(1, result)
        assertEquals(1, EventTracker.getMouseEventCount())
    }

    @Test
    fun incrementClipboardEvent_incrementsByOne() {
        val result = EventTracker.incrementClipboardEvent()
        assertEquals(1, result)
        assertEquals(1, EventTracker.getClipboardEventCount())
    }

    @Test
    fun multipleIncrements_accumulateCorrectly() {
        repeat(5) { EventTracker.incrementTouchEvent() }
        repeat(3) { EventTracker.incrementClickEvent() }
        repeat(7) { EventTracker.incrementMouseEvent() }

        assertEquals(5, EventTracker.getTouchEventCount())
        assertEquals(3, EventTracker.getClickEventCount())
        assertEquals(7, EventTracker.getMouseEventCount())
    }

    // ---- Reset tests ----

    @Test
    fun resetAllCounters_setsAllToZero() {
        repeat(10) {
            EventTracker.incrementTouchEvent()
            EventTracker.incrementClickEvent()
            EventTracker.incrementMouseEvent()
            EventTracker.incrementClipboardEvent()
        }

        EventTracker.resetAllCounters()

        assertEquals(0, EventTracker.getTouchEventCount())
        assertEquals(0, EventTracker.getClickEventCount())
        assertEquals(0, EventTracker.getMouseEventCount())
        assertEquals(0, EventTracker.getClipboardEventCount())
    }

    // ---- getAllEventCounts ----

    @Test
    fun getAllEventCounts_returnsCorrectMap() {
        repeat(2) { EventTracker.incrementTouchEvent() }
        repeat(3) { EventTracker.incrementClickEvent() }
        repeat(4) { EventTracker.incrementMouseEvent() }
        repeat(1) { EventTracker.incrementClipboardEvent() }

        val counts = EventTracker.getAllEventCounts()
        assertEquals(2, counts["touch_num_events"])
        assertEquals(3, counts["click_num_events"])
        assertEquals(4, counts["mouse_num_events"])
        assertEquals(1, counts["clipboard_num_events"])
    }

    // ---- Thread safety ----

    @Test
    fun concurrentIncrements_areThreadSafe() {
        val threadCount = 100
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(10)

        repeat(threadCount) {
            executor.submit {
                EventTracker.incrementTouchEvent()
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        assertEquals(threadCount, EventTracker.getTouchEventCount())
    }
}
