package com.tyrads.sdk.acmo.core


import AcmoKeyNames
import AcmoUsagePermissionsPage
import AcmoUsageStatsController
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.legal.AcmoPrivacyPolicyPage
import com.tyrads.sdk.acmo.modules.webview.AcmoWebView
import com.tyrads.sdk.ui.theme.TyradsSdkTheme

@Keep
class AcmoApp : ComponentActivity() {
    companion object {
        private const val ACMO_KEY_ACTIVITY_KILLED = "acmo_activity_killed"
        private const val ACMO_KEY_LANGUAGE_CHANGE = "acmo_language_change"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState?.getBoolean(ACMO_KEY_ACTIVITY_KILLED, false) == true &&
            !savedInstanceState.getBoolean(ACMO_KEY_LANGUAGE_CHANGE, false)
        ) {
            Tyrads.getInstance().log("Offerwall closed")
            finish()
            return
        }

        // Handle process death: if Tyrads is not initialized, finish the activity
        if (!Tyrads.getInstance().isInitialized()) {
            Log.e("Tyrads", "AcmoApp: Tyrads SDK is not initialized. Finishing activity.")
            finish()
            return
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        enableEdgeToEdge()
        setContent {
            TyradsSdkTheme {
                val isUsagePermissionGranted = AcmoUsageStatsController().isUsagePermissionGranted(this)
                val tyrads = Tyrads.getInstance()
                val privacyAccepted = tyrads.preferences.getBoolean(AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + Tyrads.getInstance().publisherUserID,
                    false)
                val skipInitialPages = tyrads.config.skipInitialPages

                val initPath = when {
                    skipInitialPages -> "webview"
                    !privacyAccepted -> "privacy"
                    !isUsagePermissionGranted -> "usage-permissions"
                    else -> "webview"
                }
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .statusBarsPadding(),
                    containerColor = Color.White,
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = initPath
                    ) {
                        composable("webview") {
                            AcmoWebView()
                        }
                        composable("privacy") {
                            AcmoPrivacyPolicyPage(navController = navController)
                        }
                        composable("usage-permissions") {
                            AcmoUsagePermissionsPage(
                                navController = navController
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
        outState.putBoolean(ACMO_KEY_LANGUAGE_CHANGE, true)
    }

}