package com.tyrads.sdk.acmo.modules.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.tyrads.sdk.Tyrads
import org.json.JSONException
import org.json.JSONObject
import androidx.core.net.toUri
import com.tyrads.sdk.acmo.helpers.urlsMatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Keep
@Composable
fun AcmoWebView() {
    val context = LocalContext.current
    val mUrl by Tyrads.getInstance().urlState.collectAsState()
    val webViewManager = WebViewManager.getInstance()
    val webViewState = rememberWebViewState()
    val coroutineScope = rememberCoroutineScope()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasError by remember { mutableStateOf(false) }

    val preloadedWebViewAvailable = remember { webViewManager.getHeadlessWebView() != null }
    var isLoading by remember { mutableStateOf(!preloadedWebViewAvailable) }

    var isUsingPreloadedWebView by remember { mutableStateOf(preloadedWebViewAvailable) }

    Tyrads.getInstance().log(
        "AcmoWebView: Composing - URL: $mUrl, Preload available: $preloadedWebViewAvailable",
        Log.INFO,
        force = true
    )

    val fileChooserLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val result = uri?.let { arrayOf(it) }
            webViewState.filePathCallback?.onReceiveValue(result)
            webViewState.filePathCallback = null
        }

    DisposableEffect(Unit) {
        webViewManager.onErrorChanged = { error ->
            hasError = error
        }
        onDispose {
            webViewManager.onErrorChanged = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { factoryContext ->
                Tyrads.getInstance().log("AcmoWebView: Factory executing", Log.INFO, force = true)

                // Try to get preloaded WebView
                val preloadedWebView = webViewManager.getHeadlessWebView()
                val preloadError = webViewManager.hasPreloadError()

                Tyrads.getInstance().log(
                    "AcmoWebView: Preloaded=${preloadedWebView != null}, Error=$preloadError",
                    Log.INFO,
                    force = true
                )

                val webView = if (preloadedWebView != null) {
                    Tyrads.getInstance().log(
                        "AcmoWebView: Using preloaded WebView - instant display",
                        Log.INFO,
                        force = true
                    )

                    isUsingPreloadedWebView = true

                    // Remove from container
                    (preloadedWebView.parent as? ViewGroup)?.removeView(preloadedWebView)

                    preloadedWebView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    preloadedWebView.webViewClient = AcmoWebViewClient(
                        context = factoryContext,
                        onLoadingChanged = { loading ->
                            Tyrads.getInstance().log(
                                "AcmoWebView: Preloaded WebView loading state change ignored (loading=$loading)",
                                Log.DEBUG
                            )
                        },
                        onError = { error ->
                            hasError = error
                            if (error) {
                                isUsingPreloadedWebView = false
                            }
                        }
                    )

                    preloadedWebView.webChromeClient = AcmoWebChromeClient(
                        webViewState = webViewState,
                        fileChooserLauncher = fileChooserLauncher,
                        onLoadingChanged = { loading ->
                            Tyrads.getInstance().log(
                                "AcmoWebView: Preloaded WebView progress change ignored (loading=$loading)",
                                Log.DEBUG
                            )
                        }
                    )

                    webViewManager.setActivityContext(factoryContext as? Activity)

                    Tyrads.getInstance().log(
                        "AcmoWebView: Activity context set in WebViewManager for back button",
                        Log.INFO,
                        force = true
                    )
 
                    // Only mark as fresh_preload if the preloaded URL matches our goal URL
                    // This ensures that if they don't match (e.g. stale preload or deep link race),
                    // the update block below will trigger a reload immediately.
                    val preUrl = webViewManager.getPreloadedUrl()
                    if (preUrl != null && urlsMatch(preUrl, mUrl)) {
                        Tyrads.getInstance().log("AcmoWebView: Preload matches target - marking as fresh", Log.INFO, force = true)
                        preloadedWebView.tag = "fresh_preload"
                    } else {
                        Tyrads.getInstance().log("AcmoWebView: Preload URL mismatch or missing - will reload in update", Log.WARN, force = true)
                        preloadedWebView.tag = null
                    }
 
                    // Make visible
                    preloadedWebView.visibility = android.view.View.VISIBLE
                    preloadedWebView.requestLayout()

                    webViewManager.clearPreload()
                    isLoading = false

                    preloadedWebView
                } else {
                    Tyrads.getInstance().log(
                        "AcmoWebView: No preload - creating new WebView",
                        Log.WARN,
                        force = true
                    )

                    isUsingPreloadedWebView = false

                    WebView(factoryContext).apply {
                        this.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        this.webViewClient = AcmoWebViewClient(
                            context = factoryContext,
                            onLoadingChanged = { loading -> isLoading = loading },
                            onError = { error -> hasError = error }
                        )

                        this.webChromeClient = AcmoWebChromeClient(
                            webViewState = webViewState,
                            fileChooserLauncher = fileChooserLauncher,
                            onLoadingChanged = { loading -> isLoading = loading }
                        )

                        this.settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            mediaPlaybackRequiresUserGesture = false
                            javaScriptCanOpenWindowsAutomatically = true
                        }

                        this.addJavascriptInterface(
                            WebAppInterface(factoryContext, coroutineScope),
                            "AndroidInterface"
                        )

                        Tyrads.getInstance().log(
                            "AcmoWebView: Initializing empty WebView, update block will load: $mUrl",
                            Log.INFO,
                            force = true
                        )
                    }
                }

                webViewRef = webView
                webView
            },
            update = { webView ->
                val currentUrl = webView.url
                
                // If this is a fresh preload we just attached, skip the update block
                // this once to allow the preloaded content to manifest without interference.
                if (webView.tag == "fresh_preload") {
                    Tyrads.getInstance().log("AcmoWebView: Fresh preload detected - skipping first update cycle", Log.INFO, force = true)
                    webView.tag = null
                    return@AndroidView
                }

                val wasPreloaded = webView.tag == "preloaded"

                // Check if we need to load or reload
                val needsLoad = when {
                    currentUrl == null && !wasPreloaded && !isUsingPreloadedWebView -> true
                    currentUrl != null && mUrl.isNotEmpty() && !urlsMatch(currentUrl, mUrl) -> {
                        Tyrads.getInstance().log("AcmoWebView: URL changed - reloading. Current: $currentUrl, New: $mUrl", Log.INFO, force = true)
                        true
                    }
                    else -> false
                }

                if (needsLoad) {
                    Tyrads.getInstance().log(
                        "AcmoWebView: Update block - loading URL: $mUrl",
                        Log.INFO,
                        force = true
                    )
                    isLoading = true
                    webView.loadUrl(mUrl)
                } else {
                    Tyrads.getInstance().log(
                        "AcmoWebView: Update block - skipping load (url=$currentUrl, preloaded=$wasPreloaded, usingPreloaded=$isUsingPreloadedWebView)",
                        Log.DEBUG
                    )
                }
            }
        )

        if (isLoading && !isUsingPreloadedWebView) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (hasError) {
            ErrorView(
                onRetry = {
                    hasError = false
                    webViewRef?.reload()
                }
            )
        }
    }
}

@Composable
private fun ErrorView(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Failed to load content. Please try again.",
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private class AcmoWebViewClient(
    private val context: Context,
    private val onLoadingChanged: (Boolean) -> Unit = {},
    private val onError: (Boolean) -> Unit = {}
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onLoadingChanged(true)
        onError(false)
        Tyrads.getInstance().log("AcmoWebViewClient: Page started: $url", Log.INFO, force = true)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onLoadingChanged(false)
        Tyrads.getInstance().log("AcmoWebViewClient: Page finished: $url", Log.INFO, force = true)

        // Only inject JS bridge if not already injected by preload
        // This prevents duplicate injection
        view?.evaluateJavascript(
            """
                (function() {
                    // Check if bridge already exists
                    if (window.tyradsJsBridgeInitialized) {
                        console.log('AcmoWebView: JavaScript bridge already initialized - skipping');
                        return;
                    }
                    
                    window.addEventListener('message', function(event) {
                        try {
                            const message = typeof event.data === 'string' 
                                ? JSON.parse(event.data) 
                                : event.data;
                                
                            if (message && window.AndroidInterface) {
                                AndroidInterface.postMessage(JSON.stringify(message));
                            }
                        } catch (error) {
                            console.error('Error handling message:', error);
                        }
                    });
                    
                    window.tyradsJsBridgeInitialized = true;
                    console.log('AcmoWebView: JavaScript bridge initialized');
                })();
            """.trimIndent(), null
        )
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString()
        return handleUrl(url)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return handleUrl(url)
    }

    private fun handleUrl(url: String?): Boolean {
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
                    Log.e("AcmoWebViewClient", "Failed to handle URL: $url", e)
                    false
                }
            }
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            onError(true)
            onLoadingChanged(false)
            Tyrads.getInstance().log(
                "AcmoWebViewClient: Error: ${error?.description}",
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
        onError(true)
        onLoadingChanged(false)
        Tyrads.getInstance().log(
            "AcmoWebViewClient: Error: $description",
            Log.ERROR,
            force = true
        )
    }
}

private class AcmoWebChromeClient(
    private val webViewState: WebViewState,
    private val fileChooserLauncher: ActivityResultLauncher<String>,
    private val onLoadingChanged: (Boolean) -> Unit = {}
) : WebChromeClient() {

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        webViewState.filePathCallback?.onReceiveValue(null)
        webViewState.filePathCallback = filePathCallback
        fileChooserLauncher.launch("image/*")
        return true
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onLoadingChanged(newProgress < 100)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        consoleMessage?.let {
            Tyrads.getInstance().log(
                "WebView Console: ${it.message()}",
                Log.DEBUG
            )
        }
        return true
    }
}

@Keep
class WebViewState {
    var webView: WebView? by mutableStateOf(null)
    var filePathCallback: ValueCallback<Array<Uri>>? by mutableStateOf(null)
}

@Composable
fun rememberWebViewState() = remember { WebViewState() }

@Keep
private class WebAppInterface(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postMessage(jsonMessage: String) {
        try {
            Log.d("WebAppInterface", "Received message: $jsonMessage")
            val json = JSONObject(jsonMessage)
            val action = json.optString("action")

            when (action) {
                "closeWebView" -> {
                    mainHandler.post {
                        (context as? Activity)?.finish()
                    }
                }

                "changeLanguage" -> {
                    val langCode = json.optString("value")
                    if (langCode.isNotEmpty()) {
                        mainHandler.post {
                            coroutineScope.launch {
                                Tyrads.getInstance().changeLanguage(langCode)
                            }
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