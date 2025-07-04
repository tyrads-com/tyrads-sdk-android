package com.tyrads.sdk.acmo.core.utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun isVpnActive(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
}
