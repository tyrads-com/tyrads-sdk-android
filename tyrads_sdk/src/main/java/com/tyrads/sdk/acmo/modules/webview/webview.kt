package com.tyrads.sdk.acmo.modules.webview

import AcmoUsageStatsController
import AcmoUsageStatsDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

@Keep
class WebAppInterface(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postMessage(jsonMessage: String) {
        try {
            Log.d("WebAppInterface", "Received message: $jsonMessage")
            val json = JSONObject(jsonMessage)
            val action = json.optString("action")

            when (action) {
                "closeWebview" -> {
                    mainHandler.post {
                        (context as? Activity)?.finish()
                    }
                }

                "changeLanguage" -> {
                    val langCode = json.optString("languageCode")
                    if (langCode.isNotEmpty()) {
                        mainHandler.post {
                            LocalizationHelper.changeLanguage(context, langCode, false)
                        }
                    }
                }

                else -> {
                    Log.w("WebAppInterface", "Unknown action: $action")
                }
            }
        } catch (e: JSONException) {
            Log.e("WebAppInterface", "Error parsing message: ${e.message}")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComposable(modifier: Modifier) {
    val context = LocalContext.current
    val webViewState = rememberWebViewState()
    val isLoading = remember { mutableStateOf(true) }
    val errorOccurred = remember { mutableStateOf(false) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val result = uri?.let { arrayOf(it) }
        webViewState.filePathCallback?.onReceiveValue(result)
        webViewState.filePathCallback = null
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    DisposableEffect(backDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webViewState.webView?.canGoBack() == true) {
                    webViewState.webView?.goBack()
                } else {
                    isEnabled = false
                    backDispatcher?.onBackPressed()
                }
            }
        }

        backDispatcher?.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }

    // Usage stats handling
    val usageStatsController = remember { AcmoUsageStatsController() }
    var showDialog by remember { mutableStateOf(true) }
    val status = remember { usageStatsController.checkUsagePermission() ?: false }

    val activityContext = LocalContext.current as? ComponentActivity
    val activityReference = remember { WeakReference(activityContext) }

    if (!status && showDialog) {
        Tyrads.getInstance().Dialog {
            AcmoUsageStatsDialog(
                dismissible = true,
                onDismissRequest = {
                    showDialog = false
                    CoroutineScope(Dispatchers.IO).launch {
                        usageStatsController.saveUsageStats()
                    }
                }
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    // Enable debugging in development
                    WebView.setWebContentsDebuggingEnabled(true)

                    // Configure settings
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        allowContentAccess = true
                        allowFileAccess = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
                            WebSettingsCompat.setSafeBrowsingEnabled(this, false)
                        }

                        CookieManager.getInstance().removeAllCookies(null)
//                        userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
//                        setSupportMultipleWindows(true)
                    }

                    // Add JavaScript interface
                    addJavascriptInterface(WebAppInterface(ctx), "AndroidInterface")

                    // Configure WebChromeClient
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView,
                            filePathCallback: ValueCallback<Array<Uri>>,
                            fileChooserParams: FileChooserParams
                        ): Boolean {
                            webViewState.filePathCallback = filePathCallback
                            fileChooserLauncher.launch("*/*")
                            return true
                        }

                        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                            Log.d(
                                "WebViewConsole",
                                "${consoleMessage.message()} -- " +
                                        "Line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}"
                            )
                            return true
                        }

                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            if (newProgress > 80) {
                                isLoading.value = false
                            }
                        }
                    }

                    // Configure WebViewClient
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading.value = true
                            errorOccurred.value = false
                            Log.d("WebView", "Loading URL: $url")
                            view?.evaluateJavascript(
                            """
                                    // Storage polyfills
                                    if (typeof localStorage === 'undefined') {
                                        window.localStorage = window.sessionStorage = {
                                            _data: {}, 
                                            getItem: function(k) { return this._data[k] || null; },
                                            setItem: function(k,v) { this._data[k] = v.toString(); },
                                            removeItem: function(k) { delete this._data[k]; },
                                            clear: function() { this._data = {}; }
                                        };
                                    }
                                    
                                    // CSP header removal
                                    const meta = document.createElement('meta');
                                    meta.httpEquiv = 'Content-Security-Policy';
                                    meta.content = 'default-src * \'unsafe-inline\' \'unsafe-eval\' blob: data:;';
                                    document.head.prepend(meta);
                                """, null
                            )
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading.value = false
                            Log.d("WebView", "Finished loading: $url")

                            // Inject JavaScript bridge
                            view?.evaluateJavascript(
                                """
                                if (!window.indexedDB && window.WebKitIndexedDB) {
                                    window.indexedDB = window.WebKitIndexedDB;
                                }
                                (function() {
                                    // Message event listener
                                    window.addEventListener('message', function(event) {
                                        try {
                                            const message = typeof event.data === 'string' 
                                                ? JSON.parse(event.data) 
                                                : event.data;
                                                
                                            if (message && message.command === 'webview_command') {
                                                AndroidInterface.postMessage(JSON.stringify({
                                                    command: message.command,
                                                    action: message.action,
                                                    languageCode: message.languageCode
                                                }));
                                            }
                                        } catch (error) {
                                            console.error('Error handling message:', error);
                                        }
                                    });
                                    
                                    // Debug log
                                    console.log('WebView JavaScript bridge initialized');
                                })();
                            """.trimIndent(), null
                            )
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            super.onReceivedSslError(view, handler, error)
                            Log.w(
                                "WebViewSslError",
                                "SSL Error received. Proceeding anyway. Error: ${error?.primaryError}"
                            )
                            handler?.proceed()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                errorOccurred.value = true
                                Log.e(
                                    "WebView",
                                    "Error loading main frame: ${error?.description} (Code: ${error?.errorCode})"
                                )
                            }
                        }

                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            errorResponse: WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            if (request?.isForMainFrame == true) {
                                errorOccurred.value = true
                                Log.e(
                                    "WebView",
                                    "HTTP Error: ${errorResponse?.statusCode} - ${errorResponse?.reasonPhrase}"
                                )
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString()
                            Log.d("WebView", "Intercepting URL: $url")

                            return when {
                                url == null -> false
                                url.contains("acmo-cmd") -> {
                                    if (url.contains("close-app")) {
                                        activityContext?.finish()
                                        true
                                    } else {
                                        false
                                    }
                                }

                                url.contains("websdk.tyrads.com") -> false
                                else -> {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                        true
                                    } catch (e: Exception) {
                                        Log.e("WebView", "Failed to handle URL: $url", e)
                                        false
                                    }
                                }
                            }
                        }
                    }

                    setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

                    Log.d("WebView", "Loading initial URL: ${Tyrads.getInstance().url}")
                    loadUrl(Tyrads.getInstance().url)

                    webViewState.webView = this
                }
            },
            update = { webView ->
            }
        )

        if (isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(y = 24.dp),
                    color = Color.Black,
                    strokeWidth = 3.dp
                )
            }
        }

        // Error state
        if (errorOccurred.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // You could add an error message here
            }
        }
    }

    // Clean up on dispose
    DisposableEffect(Unit) {
        onDispose {
            webViewState.webView?.let {
                it.stopLoading()
//                it.webViewClient = null
                it.webChromeClient = null
                it.removeJavascriptInterface("AndroidInterface")
                it.destroy()
                webViewState.webView = null
            }
        }
    }
}

@Keep
class WebViewState {
    var webView: WebView? by mutableStateOf(null)
    var filePathCallback: ValueCallback<Array<Uri>>? by mutableStateOf(null)
}

@Composable
fun rememberWebViewState() = remember { WebViewState() }