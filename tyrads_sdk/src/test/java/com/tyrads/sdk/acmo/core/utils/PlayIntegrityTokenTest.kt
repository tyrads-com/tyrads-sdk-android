package com.tyrads.sdk.acmo.core.utils

import android.content.Context
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenResponse
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayIntegrityTokenTest {

    private lateinit var mockContext: Context
    private lateinit var mockIntegrityManager: IntegrityManager
    private lateinit var mockTask: Task<IntegrityTokenResponse>
    private lateinit var mockResponse: IntegrityTokenResponse

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockIntegrityManager = mockk()
        mockTask = mockk()
        mockResponse = mockk()

        mockkStatic(IntegrityManagerFactory::class)
        every { IntegrityManagerFactory.create(any()) } returns mockIntegrityManager
        every { mockIntegrityManager.requestIntegrityToken(any()) } returns mockTask

        // Mock Task chain
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<IntegrityTokenResponse>>(0)
            listener.onSuccess(mockResponse)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getPlayIntegrityToken_returnsTokenOnSuccess() = runTest {
        every { mockResponse.token() } returns "mock_play_token_123"

        val token = getPlayIntegrityToken(mockContext)

        assertEquals("mock_play_token_123", token)
        verify(exactly = 1) { mockIntegrityManager.requestIntegrityToken(any()) }
    }

    @Test(expected = Exception::class)
    fun getPlayIntegrityToken_throwsExceptionOnFailure() = runTest {
        // Change mock behavior to trigger failure listener instead
        every { mockTask.addOnSuccessListener(any()) } returns mockTask
        every { mockTask.addOnFailureListener(any()) } answers {
            val listener = arg<OnFailureListener>(0)
            listener.onFailure(Exception("Play Services Error"))
            mockTask
        }

        getPlayIntegrityToken(mockContext)
    }
}
