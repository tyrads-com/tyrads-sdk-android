package com.tyrads.sdk.acmo.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri

fun acmoLaunchURL(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    context.startActivity(intent)
}