package com.tyrads.sdk.acmo.core

import AcmoUsageStatsController
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.legal.activity.AcmoPrivacyPolicyActivity
import com.tyrads.sdk.acmo.modules.legal.activity.AcmoUsagePermissionActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AcmoOnboardingGate {
    private var continuation: ( (Boolean) -> Unit )? = null

    suspend fun start(context: Context): Boolean = suspendCancellableCoroutine { cont ->
        continuation = { cont.resume(it) }
        Handler(Looper.getMainLooper()).post {
            proceed(context)
        }
        cont.invokeOnCancellation {
            continuation = null
        }
    }

    fun proceed(context: Context) {
        val tyrads = Tyrads.getInstance()

        if (!tyrads.privacyAccepted.value) {
            AcmoPrivacyPolicyActivity.start(context, true)
            return
        }

        val isUsagePermissionGranted = AcmoUsageStatsController().isUsagePermission(context)

        if (!isUsagePermissionGranted) {
            AcmoUsagePermissionActivity.start(context, true)
            return
        }

        continuation?.invoke(true)
        continuation = null
    }

    fun continueFlow(context: Context) {
        Handler(Looper.getMainLooper()).post {
            proceed(context)
        }
    }

    fun cancel() {
        continuation?.invoke(false)
        continuation = null
    }
}
