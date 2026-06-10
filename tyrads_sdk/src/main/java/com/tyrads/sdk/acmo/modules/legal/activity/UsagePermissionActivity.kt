package com.tyrads.sdk.acmo.modules.legal.activity

import AcmoUsagePermissionsPage
import AcmoUsageStatsController
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tyrads.sdk.acmo.core.AcmoOnboardingGate

class AcmoUsagePermissionActivity : ComponentActivity() {
    companion object {
        fun start(context: Context, actionType: Boolean) {
            val intent = Intent(context, AcmoUsagePermissionActivity::class.java)
            intent.putExtra("returnToWidget", actionType)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionType = intent.getBooleanExtra("returnToWidget", false)
        setContent {
            AcmoUsagePermissionsPage(
                onGrantClicked = {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
                returnToWidget = actionType
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val isUsagePermissionGranted = AcmoUsageStatsController().isUsagePermission(this)
        if (isUsagePermissionGranted) {
            finish()
            AcmoOnboardingGate.continueFlow(this)
        }
    }
}
