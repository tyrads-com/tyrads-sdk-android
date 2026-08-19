package com.tyrads.sdk.acmo.helpers

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.tyrads.sdk.Tyrads
import android.util.Log

fun launchUrlForce(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Tyrads.getInstance().log("Failed to launch URL: ${e.message}", Log.ERROR)
    }
}
