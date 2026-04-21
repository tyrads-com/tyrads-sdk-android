package com.tyrads.sdk.acmo.core.utils

import java.util.concurrent.atomic.AtomicInteger

object EventTracker {
    private val touchEventCount = AtomicInteger(0)

    private val clickEventCount = AtomicInteger(0)

    private val mouseEventCount = AtomicInteger(0)

    private val clipboardEventCount = AtomicInteger(0)

    fun incrementTouchEvent(): Int {
        return touchEventCount.incrementAndGet()
    }

    fun incrementClickEvent(): Int {
        return clickEventCount.incrementAndGet()
    }

    fun incrementMouseEvent(): Int {
        return mouseEventCount.incrementAndGet()
    }

    fun incrementClipboardEvent(): Int {
        return clipboardEventCount.incrementAndGet()
    }

    fun getTouchEventCount(): Int {
        return touchEventCount.get()
    }


    fun getClickEventCount(): Int {
        return clickEventCount.get()
    }

    fun getMouseEventCount(): Int {
        return mouseEventCount.get()
    }


    fun getClipboardEventCount(): Int {
        return clipboardEventCount.get()
    }

    @Suppress("UNUSED")
    fun resetAllCounters() {
        touchEventCount.set(0)
        clickEventCount.set(0)
        mouseEventCount.set(0)
        clipboardEventCount.set(0)
    }

    @Suppress("UNUSED")
    fun getAllEventCounts(): Map<String, Int> {
        return mapOf(
            "touch_num_events" to getTouchEventCount(),
            "click_num_events" to getClickEventCount(),
            "mouse_num_events" to getMouseEventCount(),
            "clipboard_num_events" to getClipboardEventCount()
        )
    }
}
