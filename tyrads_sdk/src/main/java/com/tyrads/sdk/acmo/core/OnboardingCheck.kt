package com.tyrads.sdk.acmo.core

import AcmoUsageStatsController
import android.content.Context
import android.util.Log
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.legal.activity.AcmoPrivacyPolicyActivity
import com.tyrads.sdk.acmo.modules.legal.activity.AcmoUsagePermissionActivity
import com.tyrads.sdk.acmo.modules.users.activity.AcmoUsersUpdateActivity

object AcmoOnboardingGate {
    private var onComplete: (() -> Unit)? = null

    fun start(context: Context, onFinished: (() -> Unit)? = null) {
        onComplete = onFinished
        if (!Tyrads.getInstance().privacyAccepted.value) {
            AcmoPrivacyPolicyActivity.start(context, true)
            return
        }

        val isUsagePermissionGranted = AcmoUsageStatsController().isUsagePermission(context)

        if (!isUsagePermissionGranted) {
            AcmoUsagePermissionActivity.start(context, true)
            return
        }

        if (Tyrads.getInstance().newUser) {
            AcmoUsersUpdateActivity.start(context, true)
            return
        }
        Log.e("AcmoGate", "All onboarding steps completed")
        onComplete?.invoke()
        onComplete = null
    }

    fun continueFlow(context: Context) {
        start(context, onComplete)
    }
}
