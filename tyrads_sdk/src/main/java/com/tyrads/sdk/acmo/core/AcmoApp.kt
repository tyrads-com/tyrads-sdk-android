package com.tyrads.sdk.acmo.core


import AcmoKeyNames
import AcmoUsagePermissionsPage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.legal.AcmoPrivacyPolicyPage
import com.tyrads.sdk.acmo.modules.webview.WebViewComposable
import com.tyrads.sdk.ui.theme.TyradsSdkTheme


class AcmoApp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        enableEdgeToEdge()
        setContent {
            TyradsSdkTheme {
                var initPath = "privacy"
                if(Tyrads.getInstance().preferences.getBoolean(AcmoKeyNames.PRIVACY_ACCEPTED,false)){
                    initPath = "webview"
                }
                Tyrads.getInstance().navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = Tyrads.getInstance().navController,
                        startDestination = initPath
                    ) {
                        composable("webview") {
                            WebViewComposable(
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        composable("privacy") {
                            AcmoPrivacyPolicyPage(
                            )
                        }
                        composable("usage-permissions") {
                            AcmoUsagePermissionsPage(
                            )
                        }
                    }
                }
            }
        }
    }
}

