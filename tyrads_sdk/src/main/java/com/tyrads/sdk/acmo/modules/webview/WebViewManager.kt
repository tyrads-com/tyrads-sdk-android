package com.tyrads.sdk.acmo.modules.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.annotation.Keep
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONException

@Keep
class WebViewManager private constructor() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: WebViewManager? = null

        @JvmStatic
        fun getInstance(): WebViewManager {
            return instance ?: synchronized(this) {
                instance ?: WebViewManager().also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var headlessWebView: WebView? = null
    private var containerView: FrameLayout? = null
    private var preloadedUrl: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getHeadlessWebView(): WebView? = headlessWebView

    @SuppressLint("SetJavaScriptEnabled")
    fun preload(context: Context, url: String) {
        Tyrads.getInstance().log("WebViewManager: preload() called with URL: $url", Log.INFO, force = true)

        // Check if already preloaded with same URL
        if (preloadedUrl == url && headlessWebView != null) {
            Tyrads.getInstance().log("WebViewManager: Already preloaded with URL: $url", Log.INFO, force = true)
            return
        }

        preloadedUrl = url

        mainHandler.post {
            try {
                disposeInternal()

                Tyrads.getInstance().log("WebViewManager: Creating headless WebView for URL: $url", Log.INFO, force = true)

                containerView = FrameLayout(context.applicationContext).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    visibility = View.GONE
                }

                headlessWebView = WebView(context.applicationContext).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Tyrads.getInstance().log("WebViewManager: Page loaded successfully: $url", Log.INFO, force = true)
                        }
                    }

                    addJavascriptInterface(
                        WebAppInterfacePreload(context, scope),
                        "AndroidInterface"
                    )

                    containerView?.addView(this)
                }

                headlessWebView?.loadUrl(url)
                Tyrads.getInstance().log("WebViewManager: Started loading URL in background", Log.INFO, force = true)

            } catch (e: Exception) {
                Tyrads.getInstance().log(
                    "WebViewManager: Error creating headless WebView: ${e.message}",
                    Log.ERROR,
                    force = true
                )
            }
        }
    }

    fun clearPreload() {
        Tyrads.getInstance().log("WebViewManager: Clearing preload reference (transfer ownership)", Log.INFO, force = true)
        headlessWebView = null
        containerView = null
        preloadedUrl = null
    }

    private fun disposeInternal() {
        try {
            mainHandler.removeCallbacksAndMessages(null)
            headlessWebView?.apply {
                stopLoading()
                clearCache(true)
                clearHistory()
                removeAllViews()
                destroy()
            }
            containerView?.removeAllViews()
            containerView = null
            headlessWebView = null
            preloadedUrl = null
            Tyrads.getInstance().log("WebViewManager: Disposed headless WebView", Log.INFO, force = true)
        } catch (e: Exception) {
            Tyrads.getInstance().log(
                "WebViewManager: Error disposing WebView: ${e.message}",
                Log.ERROR,
                force = true
            )
        }
    }

    @Keep
    private inner class WebAppInterfacePreload(
        private val context: Context,
        private val coroutineScope: CoroutineScope
    ) {
        private val mainHandler = Handler(Looper.getMainLooper())

        @JavascriptInterface
        fun postMessage(jsonMessage: String) {
            try {
                Tyrads.getInstance().log(
                    "WebAppInterfacePreload: Received: $jsonMessage",
                    Log.INFO,
                    force = true
                )

                val json = org.json.JSONObject(jsonMessage)
                val action = json.optString("action")

                if (action == "changeLanguage") {
                    val langCode = json.optString("value")
                    if (langCode.isNotEmpty()) {
                        mainHandler.post {
                            coroutineScope.launch {
                                Tyrads.getInstance().changeLanguage(langCode)
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                Tyrads.getInstance().log(
                    "WebViewManager: Error parsing message: ${e.message}",
                    Log.ERROR,
                    force = true
                )
            }
        }
    }
}