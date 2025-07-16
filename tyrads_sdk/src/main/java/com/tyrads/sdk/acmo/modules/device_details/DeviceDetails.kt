package com.tyrads.sdk.acmo.modules.device_details

import AcmoConfig
import AcmoUsageStatsController
import com.scottyab.rootbeer.RootBeer
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import DeviceInfo
import VersionInfo
import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.tyrads.sdk.acmo.core.utils.getDeviceMetrics
import com.tyrads.sdk.acmo.core.utils.getInstallerPackageName
import com.tyrads.sdk.acmo.core.utils.getNetworkSpeed
import com.tyrads.sdk.acmo.core.utils.getNetworkType
import com.tyrads.sdk.acmo.core.utils.getSystemClockInfo
import com.tyrads.sdk.acmo.core.utils.isVpnActive
import com.tyrads.sdk.acmo.core.utils.getTrackingInfo

@Keep
class AcmoDeviceDetailsController {
    private val deviceInfoLazy by lazy { acmoGetDeviceInfo(Tyrads.getInstance().context) }

    suspend fun getDeviceDetails(): Map<String, Any?> = withContext(Dispatchers.IO) {
        val context = Tyrads.getInstance().context
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val deviceInfo = deviceInfoLazy
        val deviceMetrics = getDeviceMetrics()
        val networkSpeed = getNetworkSpeed(context)
        val systemClockInfo = getSystemClockInfo()
        val isVpnActive = isVpnActive(context)
        val trackingInfo = getTrackingInfo(context)
        val installerPackageName = getInstallerPackageName(context)
        val networkType = if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_BASIC_PHONE_STATE) == PackageManager.PERMISSION_GRANTED))  {
                getNetworkType(context)
            } else {
                null
            }
        val usageController = AcmoUsageStatsController()
        val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        var deviceDetails = mapOf(
            "deviceAge" to usageController.getDeviceAgeTime(),
            "deviceId" to androidId,
            "androidId" to androidId,
            "device" to if (acmoIsTablet(context)) "tablet" else "phone",
            "brand" to deviceInfo.brand,
            "model" to deviceInfo.model,
            "manufacturer" to deviceInfo.manufacturer,
            "product" to deviceInfo.product,
            "host" to deviceInfo.host,
            "hardware" to deviceInfo.hardware,
            "serialNumber" to deviceInfo.serialNumber,
            "display" to deviceInfo.display,
            "baseOs" to deviceInfo.version.baseOS,
            "codename" to deviceInfo.version.codename,
            "sdkVersion" to AcmoConfig.SDK_VERSION,
            "releaseVersion" to deviceInfo.version.release,
            "type" to deviceInfo.type,
            "tags" to deviceInfo.tags,
            "fingerprint" to deviceInfo.fingerprint,
            "build" to packageInfo.versionCode.toString(),
            "buildSign" to packageInfo.signatures?.joinToString { it.toCharsString() },
            "version" to packageInfo.versionName,
            "package" to packageInfo.packageName,
            "platform" to AcmoConfig.SDK_PLATFORM,
            "apiVersion" to AcmoConfig.API_VERSION,
            "installerPackageName" to installerPackageName,
            "installerStore" to installerPackageName,
            "osLang" to context.resources.configuration.locales[0].language,
            "rooted" to RootBeer(context).isRooted,
            "virtual" to acmoIsEmulator,
            "sdkVersion" to AcmoConfig.SDK_VERSION,
            "sdkPlatform" to "Android",
            "deviceUpTime" to deviceMetrics["device_uptime_hours"],
            "deviceBootTime" to deviceMetrics["device_boot_time"],
            "networkSpeed" to "${networkSpeed?.get("download_speed")} KB/s",
            "systemTime" to systemClockInfo["system_time"],
            "timeZone" to systemClockInfo["time_zone_name"],
            "timeZoneOffset" to systemClockInfo["time_zone_offset"],
            "isVpnActive" to isVpnActive,
            "connectionType" to networkType,

            // Tracking Info - Telephony
            "carrierName" to trackingInfo["carrier_name"],
            "mccMnc" to trackingInfo["mcc_mnc"],
            "mcc" to trackingInfo["mcc"],
            "mnc" to trackingInfo["mnc"],
            "countryIso" to trackingInfo["country_iso"],
            "isRoaming" to trackingInfo["is_roaming"],
            "simOperatorName" to trackingInfo["sim_operator_name"],
            "simOperator" to trackingInfo["sim_operator"],
            "simCountryIso" to trackingInfo["sim_country_iso"],
            "phoneType" to trackingInfo["phone_type"],
            "supportedAbis" to trackingInfo["supported_abis"],

            // Tracking Info - CPU Information
            "cpuCores" to trackingInfo["cpu_cores"],
            "cpuType" to trackingInfo["supported_abis"],
            "supported32BitAbis" to trackingInfo["supported_32_bit_abis"],
            "supported64BitAbis" to trackingInfo["supported_64_bit_abis"],
            "cpuHardware" to trackingInfo["cpu_hardware"],
            "cpuModel" to trackingInfo["cpu_model"],
            "maxMemory" to trackingInfo["max_memory"],
            "totalMemory" to trackingInfo["total_memory"],
            "freeMemory" to trackingInfo["free_memory"],
            "osArch" to trackingInfo["os_arch"],

            // Tracking Info - Device Information
            "deviceManufacturer" to trackingInfo["device_manufacturer"],
            "deviceModel" to trackingInfo["device_model"],
            "deviceBrand" to trackingInfo["device_brand"],
            "deviceBoard" to trackingInfo["device_board"],
            "deviceHardware" to trackingInfo["device_hardware"],
            "androidVersion" to trackingInfo["android_version"],
            "androidSdkInt" to trackingInfo["android_sdk_int"],
            "buildType" to trackingInfo["build_type"],
            "buildTags" to trackingInfo["build_tags"],

            // Tracking Info - Screen Metrics
            "screenDensity" to trackingInfo["screen_density"],
            "screenWidth" to trackingInfo["screen_width"],
            "screenHeight" to trackingInfo["screen_height"]
        )
        return@withContext deviceDetails
    }


    private fun acmoGetDeviceInfo(context: Context): DeviceInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val build = mutableMapOf<String, Any>()
        build["board"] = Build.BOARD
        build["bootloader"] = Build.BOOTLOADER
        build["brand"] = Build.BRAND
        build["device"] = Build.DEVICE
        build["display"] = Build.DISPLAY
        build["fingerprint"] = Build.FINGERPRINT
        build["hardware"] = Build.HARDWARE
        build["host"] = Build.HOST
        build["id"] = Build.ID
        build["manufacturer"] = Build.MANUFACTURER
        build["model"] = Build.MODEL
        build["product"] = Build.PRODUCT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            build["supported32BitAbis"] = Build.SUPPORTED_32_BIT_ABIS.toList()
            build["supported64BitAbis"] = Build.SUPPORTED_64_BIT_ABIS.toList()
            build["supportedAbis"] = Build.SUPPORTED_ABIS.toList()
        } else {
            build["supported32BitAbis"] = emptyList<String>()
            build["supported64BitAbis"] = emptyList<String>()
            build["supportedAbis"] = emptyList<String>()
        }

        build["tags"] = Build.TAGS
        build["type"] = Build.TYPE

        val version = mutableMapOf<String, Any>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            version["baseOS"] = Build.VERSION.BASE_OS
            version["previewSdkInt"] = Build.VERSION.PREVIEW_SDK_INT
            version["securityPatch"] = Build.VERSION.SECURITY_PATCH
        }
        version["codename"] = Build.VERSION.CODENAME
        version["incremental"] = Build.VERSION.INCREMENTAL
        version["release"] = Build.VERSION.RELEASE
        version["sdkInt"] = Build.VERSION.SDK_INT
        build["version"] = version

        build["isLowRamDevice"] = activityManager.isLowRamDevice

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            build["serialNumber"] = try {
                Build.getSerial()
            } catch (ex: SecurityException) {
                Build.UNKNOWN
            }
        } else {
            build["serialNumber"] = Build.SERIAL
        }

        return DeviceInfo(
            board = build["board"] as String,
            bootloader = build["bootloader"] as String,
            brand = build["brand"] as String,
            device = build["device"] as String,
            display = build["display"] as String,
            fingerprint = build["fingerprint"] as String,
            hardware = build["hardware"] as String,
            host = build["host"] as String,
            id = build["id"] as String,
            manufacturer = build["manufacturer"] as String,
            model = build["model"] as String,
            product = build["product"] as String,
            supported32BitAbis = build["supported32BitAbis"] as List<String>,
            supported64BitAbis = build["supported64BitAbis"] as List<String>,
            supportedAbis = build["supportedAbis"] as List<String>,
            tags = build["tags"] as String,
            type = build["type"] as String,
            version = VersionInfo(
                baseOS = version["baseOS"] as String?,
                previewSdkInt = version["previewSdkInt"] as Int?,
                securityPatch = version["securityPatch"] as String?,
                codename = version["codename"] as String,
                incremental = version["incremental"] as String,
                release = version["release"] as String,
                sdkInt = version["sdkInt"] as Int
            ),
            isLowRamDevice = build["isLowRamDevice"] as Boolean,
            serialNumber = build["serialNumber"] as String
        )
    }
    // New implementation using screen size
    private fun acmoIsTablet(context: Context): Boolean {
        val screenSizeType =
            context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        return screenSizeType == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSizeType == Configuration.SCREENLAYOUT_SIZE_XLARGE
    }

    private val acmoIsEmulator: Boolean
        get() = ((Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
}
