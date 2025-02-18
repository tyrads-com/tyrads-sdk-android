package com.tyrads.sdk.acmo.core


import AcmoKeyNames
import AcmoUsagePermissionsPage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
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

@Keep
class AcmoApp : ComponentActivity() {
    companion object {
        private const val ACMO_KEY_ACTIVITY_KILLED = "acmo_activity_killed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState?.getBoolean(ACMO_KEY_ACTIVITY_KILLED, false) == true) {
            Tyrads.getInstance().log("Offerwall closed")
            finish()
            return
        }

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        enableEdgeToEdge()
        setContent {
            TyradsSdkTheme {
                Tyrads.getInstance().preferences.edit().putBoolean(AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + Tyrads.getInstance().publisherUserID, false).apply()
                var initPath = "privacy"
                if (Tyrads.getInstance().preferences.getBoolean(
                        AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + Tyrads.getInstance().publisherUserID,
                        false
                    )
                ) {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ACMO_KEY_ACTIVITY_KILLED, true)
    }
}