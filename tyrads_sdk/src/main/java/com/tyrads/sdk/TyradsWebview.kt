package com.tyrads.sdk

import AcmoUsageStatsController
import AcmoUsageStatsDialog
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.view.WindowCompat
import com.tyrads.sdk.ui.theme.TyradsSdkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TyradsWebview : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        enableEdgeToEdge()
        setContent {
            TyradsSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewComposable(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComposable(modifier: Modifier) {
    val isLoading = remember { mutableStateOf(true) }

    val url =
        "https://websdk.tyrads.com/?apiKey=${Tyrads.getInstance().apiKey}&apiSecret=${Tyrads.getInstance().apiSecret}&userID=${Tyrads.getInstance().publisherUserID}&newUser=${Tyrads.getInstance().newUser}&platform=Android&hc=${Tyrads.getInstance().loginData.data.publisherApp.headerColor}&mc=${Tyrads.getInstance().loginData.data.publisherApp.mainColor}";

    val usageStatsController = AcmoUsageStatsController()
    var showDialog by remember { mutableStateOf(true) }

    val status =
        usageStatsController.checkUsagePermission() ?: false

    val activityContext = LocalContext.current as? ComponentActivity // Get the current context
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
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.postDelayed({
                                isLoading.value = false
                            }, 0)
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
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowContentAccess = true
                    settings.allowFileAccess = true
                    settings.databaseEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    loadUrl(url)
                }
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

    }
}
