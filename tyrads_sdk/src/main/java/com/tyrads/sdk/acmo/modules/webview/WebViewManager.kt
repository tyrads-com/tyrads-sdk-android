package com.tyrads.sdk.acmo.modules.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.core.net.toUri
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
    private var hasError: Boolean = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var activityContext: android.app.Activity? = null

    var onMessage: ((String) -> Unit)? = null
    var onErrorChanged: ((Boolean) -> Unit)? = null

    fun getHeadlessWebView(): WebView? = headlessWebView

    fun getPreloadedUrl(): String? = preloadedUrl

    fun hasPreloadError(): Boolean = hasError

    fun setActivityContext(activity: android.app.Activity?) {
        activityContext = activity
        Tyrads.getInstance().log(
            "WebViewManager: Activity context set: ${activity != null}",
            Log.INFO,
            force = true
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun preload(context: Context, url: String) {
        Tyrads.getInstance().log("WebViewManager: preload() called with URL: $url", Log.INFO, force = true)

        // Check if already preloaded with same URL
        if (preloadedUrl == url && headlessWebView != null) {
            Tyrads.getInstance().log("WebViewManager: Already preloaded with URL: $url", Log.INFO, force = true)
            return
        }

        preloadedUrl = url
        hasError = false

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
                        mediaPlaybackRequiresUserGesture = false
                        javaScriptCanOpenWindowsAutomatically = true
                        allowUniversalAccessFromFileURLs = false
                        allowFileAccessFromFileURLs = false
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val requestUrl = request?.url?.toString()
                            return handleUrlOverride(context, requestUrl)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            return handleUrlOverride(context, url)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            hasError = false
                            onErrorChanged?.invoke(false)
                            Tyrads.getInstance().log("WebViewManager: Page loading started: $url", Log.INFO, force = true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Tyrads.getInstance().log("WebViewManager: Page loaded successfully: $url", Log.INFO, force = true)

                            // Inject JavaScript bridge once - will be used by the visible WebView
                            view?.evaluateJavascript(
                                """
                                (function() {
                                    if (window.tyradsJsBridgeInitialized) {
                                        console.log('WebViewManager: Bridge already initialized - skipping');
                                        return;
                                    }
                                    window.addEventListener('message', function(event) {
                                        try {
                                            const message = typeof event.data === 'string' 
                                                ? JSON.parse(event.data) 
                                                : event.data;
                                                
                                            if (message && window.AndroidInterface) {
                                                console.log('WebViewManager JS Bridge Message:', message);
                                                AndroidInterface.postMessage(JSON.stringify(message));
                                            }
                                        } catch (error) {
                                            console.error('WebViewManager JS Bridge error:', error);
                                        }
                                    });
                                    window.tyradsJsBridgeInitialized = true;
                                    console.log('WebViewManager: JavaScript bridge initialized');
                                })();
                                """.trimIndent(), null
                            )
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                onErrorChanged?.invoke(true)
                                Tyrads.getInstance().log(
                                    "WebViewManager: Error loading page: ${error?.description}",
                                    Log.ERROR,
                                    force = true
                                )
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            hasError = true
                            onErrorChanged?.invoke(true)
                            Tyrads.getInstance().log(
                                "WebViewManager: Error loading page: $description",
                                Log.ERROR,
                                force = true
                            )
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage?.let {
                                Tyrads.getInstance().log("WebViewManager Console: ${it.message()}", Log.DEBUG)
                            }
                            return true
                        }

                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            Tyrads.getInstance().log("WebViewManager: Loading progress: $newProgress%", Log.DEBUG)
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
                Log.e("WebViewManager", "Error creating headless WebView", e)
                hasError = true
                onErrorChanged?.invoke(true)
            }
        }
    }

    private fun handleUrlOverride(context: Context, url: String?): Boolean {
        return when {
            url == null -> false
            url.contains("sdk.tyrads.com") -> false
            else -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    true
                } catch (e: Exception) {
                    Log.e("WebViewManager", "Failed to handle URL: $url", e)
                    false
                }
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
            hasError = false
            activityContext = null
            Tyrads.getInstance().log("WebViewManager: Disposed headless WebView", Log.INFO, force = true)
        } catch (e: Exception) {
            Tyrads.getInstance().log(
                "WebViewManager: Error disposing WebView: ${e.message}",
                Log.ERROR,
                force = true
            )
        }
    }

    fun dispose() {
        mainHandler.post {
            disposeInternal()
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
                    "🔔 WebAppInterfacePreload: Received: $jsonMessage",
                    Log.INFO,
                    force = true
                )

                // Parse the message
                val json = org.json.JSONObject(jsonMessage)
                val action = json.optString("action")

                // Handle closeWebView action
                if (action == "closeWebView") {
                    Tyrads.getInstance().log(
                        "🔔 WebAppInterfacePreload: closeWebView action detected",
                        Log.INFO,
                        force = true
                    )

                    mainHandler.post {
                        // Use activityContext if available (when WebView is visible in Activity)
                        val activity = activityContext ?: (context as? android.app.Activity)

                        if (activity != null && !activity.isFinishing) {
                            Tyrads.getInstance().log(
                                "✅ WebAppInterfacePreload: Closing activity",
                                Log.INFO,
                                force = true
                            )
                            activity.finish()
                        } else {
                            Tyrads.getInstance().log(
                                "⚠️ WebAppInterfacePreload: No Activity context available",
                                Log.WARN,
                                force = true
                            )
                            onMessage?.invoke(jsonMessage)
                        }
                    }
                } else if (action == "changeLanguage") {
                    val langCode = json.optString("value")
                    if (langCode.isNotEmpty()) {
                        mainHandler.post {
                            coroutineScope.launch {
                                Tyrads.getInstance().changeLanguage(langCode)
                            }
                        }
                    }
                } else {
                    // Forward other messages to callback
                    onMessage?.invoke(jsonMessage)
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