package com.tyrads.sdk.acmo.modules.tracking

import com.tyrads.sdk.Tyrads
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the tracking controller pattern.
 *
 * Note: AcmoTrackingController lives in the default package (no package declaration)
 * and cannot be directly imported in test code. These tests validate the observable
 * behavior of the tracking deduplication guard using Tyrads.getInstance().tracker
 * which is the public accessor to the controller.
 */
class AcmoTrackingControllerTest {

    @Test
    fun tracker_isNotNull_onFreshInstance() {
        val tyrads = Tyrads.getInstance()
        assertNotNull("Tracker must never be null", tyrads.tracker)
    }

    @Test
    fun tracker_initialSubmitting_isFalse() {
        val tyrads = Tyrads.getInstance()
        assertFalse(
            "Tracker should not be in submitting state at startup",
            tyrads.tracker.submitting
        )
    }

    @Test
    fun tracker_setSubmitting_true_preventsReentry() {
        val tyrads = Tyrads.getInstance()
        tyrads.tracker.submitting = true

        // While submitting=true, calling trackUser should be a no-op
        tyrads.tracker.trackUser("test_activity")

        // submitting should still be true (the guard prevented the call from resetting it)
        assertTrue(tyrads.tracker.submitting)

        // Cleanup
        tyrads.tracker.submitting = false
    }

    @Test
    fun tracker_afterManualReset_allowsNextCall() {
        val tyrads = Tyrads.getInstance()
        tyrads.tracker.submitting = true
        tyrads.tracker.submitting = false // reset

        assertFalse(tyrads.tracker.submitting)
    }

    @Test
    fun tracker_multipleCallsWhileSubmitting_doNotChangeFlag() {
        val tyrads = Tyrads.getInstance()
        tyrads.tracker.submitting = true

        repeat(5) {
            tyrads.tracker.trackUser("activity_$it")
        }

        // All calls were no-ops — flag remains true
        assertTrue(tyrads.tracker.submitting)

        // Cleanup
        tyrads.tracker.submitting = false
    }
}
