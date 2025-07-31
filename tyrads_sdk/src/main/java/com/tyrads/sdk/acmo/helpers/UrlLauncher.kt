package com.tyrads.sdk.acmo.helpers

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun launchUrlForce(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
