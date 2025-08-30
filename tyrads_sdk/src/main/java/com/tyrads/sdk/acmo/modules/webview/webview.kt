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
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper
import org.json.JSONException
import org.json.JSONObject
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@SuppressLint("SetJavaScriptEnabled", "ContextCastToActivity")
@Keep
@Composable
fun AcmoWebView() {
    val context = LocalContext.current
    val mUrl = Tyrads.getInstance().url
    val webViewState = rememberWebViewState()
    val coroutineScope = rememberCoroutineScope()

    val fileChooserLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val result = uri?.let { arrayOf(it) }
            webViewState.filePathCallback?.onReceiveValue(result)
            webViewState.filePathCallback = null
        }

    val usageStatsController = AcmoUsageStatsController()
    var showDialog by remember { mutableStateOf(true) }

    val status = usageStatsController.checkUsagePermission()

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

    AndroidView(factory = {
        WebView(it).apply {
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.webViewClient = AcmoWebViewClient(context)
            this.webChromeClient = AcmoWebChromeClient(
                webViewState = webViewState,
                fileChooserLauncher = fileChooserLauncher
            )
            this.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
            }
            this.addJavascriptInterface(WebAppInterface(it, coroutineScope), "AndroidInterface")
        }
    }, update = {
        it.loadUrl(mUrl)
    })
}

private class AcmoWebViewClient(private val context: Context) : WebViewClient() {
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
        Log.d("WebView", "Intercepting URL: $url")

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
                    Log.e("WebView", "Failed to handle URL: $url", e)
                    false
                }
            }
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view?.evaluateJavascript(
            """
                (function() {
                    window.addEventListener('message', function(event) {
                        try {
                            const message = typeof event.data === 'string' 
                                ? JSON.parse(event.data) 
                                : event.data;
                                
                            if (message) {
                                AndroidInterface.postMessage(JSON.stringify(message));
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
}

private class AcmoWebChromeClient(
    private val webViewState: WebViewState,
    private val fileChooserLauncher: ActivityResultLauncher<String>
) : WebChromeClient() {

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        webViewState.filePathCallback?.onReceiveValue(null) // cancel old request if still open
        webViewState.filePathCallback = filePathCallback
        fileChooserLauncher.launch("*/*")
        return true
    }
}


@Keep
private class WebAppInterface(private val context: Context, private val coroutineScope: CoroutineScope) {
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
                                Tyrads.getInstance().changeLanguage(  langCode)
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

@Keep
class WebViewState {
    var webView: WebView? by mutableStateOf(null)
    var filePathCallback: ValueCallback<Array<Uri>>? by mutableStateOf(null)

}

@Composable
fun rememberWebViewState() = remember { WebViewState() }

