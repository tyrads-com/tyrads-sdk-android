package com.tyrads.sdk

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DefaultBody
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.URL

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkCommonsTest {

    private lateinit var originalClient: Client
    private lateinit var mockClient: Client
    private lateinit var networkCommons: NetworkCommons

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        // Save the original client to restore it later to avoid affecting other tests
        originalClient = FuelManager.instance.client
        mockClient = mockk()
        FuelManager.instance.client = mockClient

        // Initialize Tyrads to prevent NPEs in NetworkCommons init block
        mockkObject(Tyrads)
        val mockTyrads = mockk<Tyrads>(relaxed = true)
        val mockPrefs = mockk<android.content.SharedPreferences>(relaxed = true)
        every { mockTyrads.preferences } returns mockPrefs
        every { Tyrads.getInstance() } returns mockTyrads

        networkCommons = NetworkCommons()
    }

    @After
    fun tearDown() {
        FuelManager.instance.client = originalClient
        unmockkAll()
    }

    private fun mockJsonResponse(json: String, statusCode: Int = 200) {
        val byteArray = json.toByteArray()
        val mockResponse = Response(
            url = URL("https://test.com"),
            statusCode = statusCode,
            responseMessage = if (statusCode in 200..299) "OK" else "Error",
            headers = Headers(),
            contentLength = byteArray.size.toLong(),
            body = DefaultBody.from({ byteArray.inputStream() }, { byteArray.size.toLong() })
        )
        every { mockClient.executeRequest(any()) } returns mockResponse
        
        try {
            // Fuel coroutines might add awaitRequest as an extension or interface method
            coEvery { mockClient.executeRequest(any()) } returns mockResponse
            // This is a bit of a hack using MockK reflection for suspend funcs
            coEvery { mockClient.invoke("awaitRequest").withArguments(any()) } returns mockResponse
        } catch (e: Exception) {}
        
        // Let's just mock it directly if the interface has it
        coEvery { mockClient.awaitRequest(any()) } returns mockResponse
    }

    @Test
    fun isNewUser_returnsTrueWhenProfileIsIncomplete() = runTest {
        val json = """
            {
                "data": {
                    "age": false,
                    "gender": true
                }
            }
        """.trimIndent()
        mockJsonResponse(json)

        val isNew = networkCommons.isNewUser()
        assertTrue(isNew)
    }

    @Test
    fun isNewUser_returnsFalseWhenProfileIsComplete() = runTest {
        val json = """
            {
                "data": {
                    "age": true,
                    "gender": true
                }
            }
        """.trimIndent()
        mockJsonResponse(json)

        val isNew = networkCommons.isNewUser()
        assertFalse(isNew)
    }

    @Test
    fun fetchCampaigns_sortsAndFiltersCorrectly() = runTest {
        // Provide 3 campaigns. One has 0 payout (should be filtered),
        // One has high premium, one has low premium.
        val json = """
            {
                "data": [
                    {
                        "campaignId": 1,
                        "campaignPremium": false,
                        "payoutSummary": {
                            "USD": { "totalPlayablePayoutConverted": 10.0 }
                        },
                        "availableCurrencies": {
                            "USD": { "currencyId": 1 }
                        }
                    },
                    {
                        "campaignId": 2,
                        "campaignPremium": true,
                        "payoutSummary": {
                            "USD": { "totalPlayablePayoutConverted": 5.0 }
                        },
                        "availableCurrencies": {
                            "USD": { "currencyId": 1 }
                        }
                    },
                    {
                        "campaignId": 3,
                        "campaignPremium": false,
                        "payoutSummary": {
                            "USD": { "totalPlayablePayoutConverted": 0.0 }
                        },
                        "availableCurrencies": {
                            "USD": { "currencyId": 1 }
                        }
                    }
                ]
            }
        """.trimIndent()
        mockJsonResponse(json)

        val campaigns = networkCommons.fetchCampaigns("en")

        // Should filter out the 0 payout (campaignId 3)
        // Should sort by premium descending (true first), so campaignId 2 then campaignId 1
        assertEquals(2, campaigns.size)
        assertEquals(2, campaigns[0].campaignId)
        assertEquals(1, campaigns[1].campaignId)
    }

    @Test
    fun fetchActiveOffersSummary_returnsActiveCampaignCount() = runTest {
        val json = """
            {
                "data": {
                    "activeCampaignCount": 42
                }
            }
        """.trimIndent()
        mockJsonResponse(json)

        val count = networkCommons.fetchActiveOffersSummary("en")
        assertEquals(42, count)
    }
}
