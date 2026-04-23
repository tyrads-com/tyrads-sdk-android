package com.tyrads.sdk.acmo.modules.webview

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class WebViewManagerInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Ensure clean state before each test
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            WebViewManager.getInstance().dispose()
        }
    }

    @Test
    fun preload_storesUrlAndCreatesWebView() {
        val testUrl = "https://v4.sdk.tyrads.com/?to=home"
        
        WebViewManager.getInstance().preload(context, testUrl)
        
        // Preload uses mainHandler.post, so we need to wait for the UI thread
        val latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post { latch.countDown() }
        latch.await(2, TimeUnit.SECONDS)

        assertEquals(testUrl, WebViewManager.getInstance().getPreloadedUrl())
        
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertNotNull(WebViewManager.getInstance().getHeadlessWebView())
        }
    }

    @Test
    fun clearPreload_removesUrlAndWebViewReferences() {
        val testUrl = "https://v4.sdk.tyrads.com/?to=home"
        
        WebViewManager.getInstance().preload(context, testUrl)
        
        val latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post { latch.countDown() }
        latch.await(2, TimeUnit.SECONDS)

        WebViewManager.getInstance().clearPreload()
        
        assertNull(WebViewManager.getInstance().getPreloadedUrl())
        assertNull(WebViewManager.getInstance().getHeadlessWebView())
    }

    @Test
    fun dispose_destroysWebViewProperly() {
        val testUrl = "https://v4.sdk.tyrads.com/?to=home"
        
        WebViewManager.getInstance().preload(context, testUrl)
        
        var latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post { latch.countDown() }
        latch.await(2, TimeUnit.SECONDS)

        WebViewManager.getInstance().dispose()
        
        latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post { latch.countDown() }
        latch.await(2, TimeUnit.SECONDS)

        assertNull(WebViewManager.getInstance().getPreloadedUrl())
        assertNull(WebViewManager.getInstance().getHeadlessWebView())
        assertFalse(WebViewManager.getInstance().hasPreloadError())
    }
}
