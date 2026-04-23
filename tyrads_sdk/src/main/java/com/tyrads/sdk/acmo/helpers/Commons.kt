package com.tyrads.sdk.acmo.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import androidx.core.net.toUri

@Keep
fun acmoLaunchURL(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    context.startActivity(intent)
}

@Keep
fun isGooglePlayServicesAvailable(context: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return resultCode == ConnectionResult.SUCCESS
}

@Keep
fun urlsMatch(url1: String, url2: String): Boolean {
    return try {
        val uri1 = url1.toUri()
        val uri2 = url2.toUri()
        
        val to1 = uri1.getQueryParameter("to") ?: ""
        val to2 = uri2.getQueryParameter("to") ?: ""
        
        if (url1 == url2) return true
        uri1.host != null && uri1.host == uri2.host && to1 == to2
    } catch (e: Exception) {
        url1 == url2
    }
}