package com.tyrads.sdk.acmo.modules.usage_stats

import AcmoEndpointNames
import AcmoUsageStatRepository
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.tyrads.sdk.Tyrads
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import java.net.URL

@OptIn(ExperimentalCoroutinesApi::class)
class AcmoUsageStatRepositoryTest {

    private lateinit var mockTyrads: Tyrads
    private lateinit var repository: AcmoUsageStatRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = AcmoUsageStatRepository()

        mockkObject(Tyrads)
        mockTyrads = mockk(relaxed = true)
        every { Tyrads.getInstance() } returns mockTyrads
        every { mockTyrads.encKey } returns "test_encryption_key_123456789012"
        
        mockkObject(Fuel)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun saveUsageStats_whenSecure_encryptsDataAndPosts() = runTest {
        every { mockTyrads.isSecure } returns true

        val mockRequest = mockk<Request>(relaxed = true)
        every { Fuel.post(AcmoEndpointNames.USAGE_STATS) } returns mockRequest
        every { mockRequest.body(any<String>()) } returns mockRequest

        val successResult = Result.Success(ByteArray(0))
        val mockResponse = mockk<Response>()
        every { mockRequest.response() } returns Triple(mockRequest, mockResponse, successResult)

        val testData = mapOf("test" to "data")
        val result = repository.saveUsageStats(testData)

        assertArrayEquals(successResult.value, result as ByteArray)
        
        val bodySlot = slot<String>()
        verify(exactly = 1) { mockRequest.body(capture(bodySlot)) }
        // When encrypted, the original 'test' key will be obscured in the JSON map
        val capturedBody = bodySlot.captured
        assert(capturedBody.contains("\"val\""))
        assert(capturedBody.contains("\"vec\""))
    }

    @Test
    fun saveUsageStats_whenNotSecure_postsRawData() = runTest {
        every { mockTyrads.isSecure } returns false

        val mockRequest = mockk<Request>(relaxed = true)
        every { Fuel.post(AcmoEndpointNames.USAGE_STATS) } returns mockRequest
        every { mockRequest.body(any<String>()) } returns mockRequest

        val successResult = Result.Success(ByteArray(0))
        val mockResponse = mockk<Response>()
        every { mockRequest.response() } returns Triple(mockRequest, mockResponse, successResult)

        val testData = mapOf("test" to "data")
        val result = repository.saveUsageStats(testData)

        assertArrayEquals(successResult.value, result as ByteArray)
        
        val bodySlot = slot<String>()
        verify(exactly = 1) { mockRequest.body(capture(bodySlot)) }
        // When not secure, the raw JSON payload should be visible
        assert(bodySlot.captured.contains("\"test\":\"data\""))
    }

    @Test(expected = FuelError::class)
    fun saveUsageStats_throwsErrorOnFailure() = runTest {
        every { mockTyrads.isSecure } returns false

        val mockRequest = mockk<Request>(relaxed = true)
        every { Fuel.post(AcmoEndpointNames.USAGE_STATS) } returns mockRequest
        every { mockRequest.body(any<String>()) } returns mockRequest

        val fuelError = FuelError.wrap(Exception("Network error"))
        val failureResult = Result.Failure(fuelError)
        val mockResponse = mockk<Response>()
        every { mockRequest.response() } returns Triple(mockRequest, mockResponse, failureResult)

        repository.saveUsageStats(mapOf())
    }
}
