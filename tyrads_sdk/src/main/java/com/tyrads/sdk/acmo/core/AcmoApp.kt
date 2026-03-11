package com.tyrads.sdk.acmo.core


import AcmoKeyNames
import AcmoUsagePermissionsPage
import AcmoUsageStatsController
import AcmoUsersUpdatePage
import android.content.Context
import android.os.Bundle
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
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper
import com.tyrads.sdk.acmo.modules.legal.AcmoPrivacyPolicyPage
import com.tyrads.sdk.acmo.modules.webview.AcmoWebView
import com.tyrads.sdk.ui.theme.TyradsSdkTheme

@Keep
class AcmoApp : ComponentActivity() {
    companion object {
        private const val ACMO_KEY_ACTIVITY_KILLED = "acmo_activity_killed"
        private const val ACMO_KEY_LANGUAGE_CHANGE = "acmo_language_change"

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalizationHelper.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Guard against Android process death: if the OS killed the process and is
        // restoring this Activity from the back stack, Tyrads.init() has NOT been
        // called in this new process. Accessing `preferences` (a lateinit var) would
        // crash with UninitializedPropertyAccessException. Detect and finish cleanly.
        if (!Tyrads.getInstance().isInitialized) {
            finish()
            return
        }

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
        LocalizationHelper.applySavedLanguage(this)

        enableEdgeToEdge()
        setContent {
            TyradsSdkTheme {
                var initPath = "privacy"
                val isUsagePermissionGranted = AcmoUsageStatsController().isUsagePermissionGranted(this)
                if (Tyrads.getInstance().preferences.getBoolean(
                        AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + Tyrads.getInstance().publisherUserID,
                        false
                    )
                ) {
                    initPath =
                        if (!isUsagePermissionGranted) "usage-permissions" else if (Tyrads.getInstance().newUser) "update-user" else "webview"
                }
                Tyrads.getInstance().navController = rememberNavController()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .statusBarsPadding(),
                    containerColor = Color.White,
                ) { innerPadding ->
                    NavHost(
                        navController = Tyrads.getInstance().navController,
                        startDestination = initPath
                    ) {
                        composable("webview") {
                            AcmoWebView()
                        }
                        composable("privacy") {
                            AcmoPrivacyPolicyPage()
                        }
                        composable("update-user") {
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