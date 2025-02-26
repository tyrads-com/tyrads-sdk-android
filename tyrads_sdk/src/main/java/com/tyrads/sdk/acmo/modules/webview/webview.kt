package com.tyrads.sdk.acmo.modules.webview

import AcmoUsageStatsController
import AcmoUsageStatsDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import java.lang.ref.WeakReference

class WebAppInterface(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    @JavascriptInterface
    fun changeLanguage(langCode: String?) {
        try {
            if (langCode == null) {
                return
            }
            mainHandler.post {
                LocalizationHelper.changeLanguage(context, langCode, shouldRecreate = false)
            }
        } catch (e: Exception) {
            Log.e("WebAppInterface", "Error changing language: ${e.message}")
        }
    }
}
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComposable(modifier: Modifier) {
    val webViewState = rememberWebViewState()
    
    val fileChooserLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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

    val isLoading = remember { mutableStateOf(true) }

    val usageStatsController = AcmoUsageStatsController()
    var showDialog by remember { mutableStateOf(true) }

    val status = usageStatsController.checkUsagePermission() ?: false

    val activityContext = LocalContext.current as? ComponentActivity
    val activityReference = WeakReference(activityContext)
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
        var webView: WebView? = null
        var handler: Handler? = null
        var runnableCode: Runnable? = null

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                webView = WebView(context).apply {
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
                    }
                    addJavascriptInterface(WebAppInterface(context), "AndroidInterface")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.postDelayed({
                                isLoading.value = false
                            }, 0)
                            evaluateJavascript("""
                                 window.addEventListener('message', function(event) {
                                    try {
                                        const message = JSON.parse(event.data);
                                        if (message && message.action === 'changeLanguage') {
                                            AndroidInterface.changeLanguage(message.languageCode);
                                        }
                                    } catch (error) {
                                        console.error('Error handling message:', error);
                                    }
                                });
                            """.trimIndent(), null)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            request?.url?.toString()?.let { url ->
                                when {
                                    url.contains("acmo-cmd") -> {
                                        if (url.contains("close-app")) {
                                            activityContext?.finish()
                                            return true
                                        }
                                    }

                                    url.contains("websdk.tyrads.com") -> {
                                        return false
                                    }

                                    else -> {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                        return true

                                    }
                                }
                            }
                            return false
                        }
                    }
                    settings.apply {
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        domStorageEnabled = true
                        allowContentAccess = true
                        allowFileAccess = true
                        javaScriptEnabled = true
                        databaseEnabled = true
                        textZoom = 100
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS

                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    loadUrl(Tyrads.getInstance().url.toString())
                    webViewState.webView = this

                    // Initialize handler and runnable
                    // handler = Handler(Looper.getMainLooper())
                    // runnableCode = object : Runnable {
                    //     override fun run() {
                    //         clearCache(true)
                    //         handler?.postDelayed(this, 15 * 60 * 1000) // Run every 5 minutes
                    //     }
                    // }
                    // handler?.post(runnableCode!!)
                }
                webView!!
            }
        )

        // Dispose of the WebView and Handler when the composable is removed from the composition
        DisposableEffect(Unit) {
            onDispose {
                // Stop the handler from running callbacks
                // handler?.removeCallbacks(runnableCode!!)
                // handler = null
                // runnableCode = null

                webView?.let {
                    it.stopLoading()
                    it.clearHistory()
                    it.removeAllViews()
                    it.destroy()
                    webView = null
                }
            }
        }

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

    }
}
@Keep
class WebViewState {
    var webView: WebView? by mutableStateOf(null)
    var filePathCallback: ValueCallback<Array<Uri>>? by mutableStateOf(null)

}
@Composable
fun rememberWebViewState() = remember { WebViewState() }

