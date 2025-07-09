package com.tyrads.sdk.acmo.core.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission

@RequiresPermission(anyOf = [Manifest.permission.READ_BASIC_PHONE_STATE, Manifest.permission.READ_PHONE_STATE])
fun getNetworkType(context: Context): String {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val network = connectivityManager.activeNetwork ?: return "No Connection"
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"

    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            val networkType =
                telephonyManager.dataNetworkType

            when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "Mobile - 5G"
                TelephonyManager.NETWORK_TYPE_LTE -> "Mobile - 4G LTE"
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_EVDO_B -> "Mobile - 3G"
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT -> "Mobile - 2G"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Mobile - Unknown Type"
                else -> "Mobile Data"
            }
        }
        else -> "Unknown"
    }
}
fun getNetworkSpeed(context: Context): Map<String, Any>? {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    val downloadSpeed = capabilities?.linkDownstreamBandwidthKbps ?: 0
    val uploadSpeed = capabilities?.linkUpstreamBandwidthKbps ?: 0
    return mapOf(
        "download_speed" to downloadSpeed/8,
        "upload_speed" to uploadSpeed/8
    )
}
