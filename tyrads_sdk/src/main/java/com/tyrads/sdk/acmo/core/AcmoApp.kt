package com.tyrads.sdk.acmo.core


import AcmoUsagePermissionsPage
import AcmoUsageStatsController
import AcmoUsersUpdatePage
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.legal.AcmoPrivacyPolicyPage
import com.tyrads.sdk.acmo.modules.webview.AcmoWebView
import com.tyrads.sdk.ui.theme.TyradsSdkTheme
import androidx.compose.runtime.collectAsState

@Keep
class AcmoApp : ComponentActivity() {
    companion object {
        private const val ACMO_KEY_ACTIVITY_KILLED = "acmo_activity_killed"
        private const val ACMO_KEY_LANGUAGE_CHANGE = "acmo_language_change"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Tyrads.getInstance().initializePrivacyStatus()

        if (savedInstanceState?.getBoolean(ACMO_KEY_ACTIVITY_KILLED, false) == true &&
            !savedInstanceState.getBoolean(ACMO_KEY_LANGUAGE_CHANGE, false)
        ) {
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
                val tyrads = Tyrads.getInstance()
                val isUsagePermissionGranted = AcmoUsageStatsController().isUsagePermission(this)
                val privacyAccepted = tyrads.privacyAccepted.collectAsState().value
                Log.e("Config", tyrads.tyradsConfig.skipInitialPages.toString())

                val initPath = when {
                    privacyAccepted && !isUsagePermissionGranted -> "usage-permissions"
                    privacyAccepted && tyrads.newUser -> "users-update"
                    privacyAccepted -> "webview"

                    tyrads.tyradsConfig.skipInitialPages && tyrads.newUser -> "users-update"
                    tyrads.tyradsConfig.skipInitialPages -> "webview"

                    else -> "privacy"
                }

                Tyrads.getInstance().navController = rememberNavController()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    NavHost(
                        navController = Tyrads.getInstance().navController,
                        startDestination = initPath
                    ) {
                        composable("webview") {
                            AcmoWebView()
                        }
                        composable("privacy") {
                            AcmoPrivacyPolicyPage(
                            )
                        }
                        composable("users-update") {
                            AcmoUsersUpdatePage()
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
        outState.putBoolean(ACMO_KEY_LANGUAGE_CHANGE, true)
    }
}