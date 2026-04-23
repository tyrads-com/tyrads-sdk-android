package com.tyrads.sdk.acmo.core

import AcmoUsageStatsController
import android.content.Context
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.TyradsConfig
import com.tyrads.sdk.acmo.modules.legal.activity.AcmoPrivacyPolicyActivity
import com.tyrads.sdk.acmo.modules.legal.activity.AcmoUsagePermissionActivity
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OnboardingCheckTest {

    private lateinit var mockContext: Context
    private lateinit var mockTyrads: Tyrads
    private lateinit var mockConfig: TyradsConfig
    private lateinit var privacyStateFlow: MutableStateFlow<Boolean>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockContext = mockk(relaxed = true)
        
        mockkObject(Tyrads)
        mockTyrads = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)
        privacyStateFlow = MutableStateFlow(false)

        every { Tyrads.getInstance() } returns mockTyrads
        every { mockTyrads.tyradsConfig } returns mockConfig
        every { mockTyrads.privacyAccepted } returns privacyStateFlow

        mockkObject(AcmoPrivacyPolicyActivity)
        every { AcmoPrivacyPolicyActivity.start(any(), any()) } just Runs

        mockkObject(AcmoUsagePermissionActivity)
        every { AcmoUsagePermissionActivity.start(any(), any()) } just Runs

        mockkConstructor(AcmoUsageStatsController::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun start_whenSkipInitialPagesIsTrue_invokesOnFinishedImmediately() {
        every { mockConfig.skipInitialPages } returns true
        
        var finishedCalled = false
        AcmoOnboardingGate.start(mockContext) {
            finishedCalled = true
        }

        assertTrue(finishedCalled)
        verify(exactly = 0) { AcmoPrivacyPolicyActivity.start(any(), any()) }
        verify(exactly = 0) { AcmoUsagePermissionActivity.start(any(), any()) }
    }

    @Test
    fun start_whenPrivacyNotAccepted_launchesPrivacyPolicyActivity() {
        every { mockConfig.skipInitialPages } returns false
        privacyStateFlow.value = false
        
        var finishedCalled = false
        AcmoOnboardingGate.start(mockContext) {
            finishedCalled = true
        }

        assertTrue(!finishedCalled)
        verify(exactly = 1) { AcmoPrivacyPolicyActivity.start(mockContext, true) }
        verify(exactly = 0) { AcmoUsagePermissionActivity.start(any(), any()) }
    }

    @Test
    fun start_whenPrivacyAcceptedButUsagePermissionNotGranted_launchesUsagePermissionActivity() {
        every { mockConfig.skipInitialPages } returns false
        privacyStateFlow.value = true
        every { anyConstructed<AcmoUsageStatsController>().isUsagePermission(any()) } returns false
        
        var finishedCalled = false
        AcmoOnboardingGate.start(mockContext) {
            finishedCalled = true
        }

        assertTrue(!finishedCalled)
        verify(exactly = 0) { AcmoPrivacyPolicyActivity.start(any(), any()) }
        verify(exactly = 1) { AcmoUsagePermissionActivity.start(mockContext, true) }
    }

    @Test
    fun start_whenAllPermissionsGranted_invokesOnFinishedImmediately() {
        every { mockConfig.skipInitialPages } returns false
        privacyStateFlow.value = true
        every { anyConstructed<AcmoUsageStatsController>().isUsagePermission(any()) } returns true
        
        var finishedCalled = false
        AcmoOnboardingGate.start(mockContext) {
            finishedCalled = true
        }

        assertTrue(finishedCalled)
        verify(exactly = 0) { AcmoPrivacyPolicyActivity.start(any(), any()) }
        verify(exactly = 0) { AcmoUsagePermissionActivity.start(any(), any()) }
    }
}
