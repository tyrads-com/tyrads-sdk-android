package com.example.tyrads_sdk_gitlab.acmo.modules.device_details

import AcmoConfig
import AcmoUsageStatsController
import com.scottyab.rootbeer.RootBeer
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import DeviceInfo
import VersionInfo
import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.provider.Settings
import androidx.annotation.Keep

@Keep
class AcmoDeviceDetailsController() {

    suspend fun getDeviceDetails(): Map<String, Any?> = withContext(Dispatchers.IO) {
        var context = Tyrads.getInstance().context
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val deviceInfo = acmoGetDeviceInfo(context)
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

            "installerPackageName" to (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }),
            "osLang" to context.resources.configuration.locales[0].language,
            "rooted" to RootBeer(context).isRooted,
            "virtual" to acmoIsEmulator,
            "sdkVersion" to AcmoConfig.SDK_VERSION,
            "sdkPlatform" to "Android"
        )
        return@withContext deviceDetails
    }

    fun acmoGetDeviceInfo(context: Context): DeviceInfo {
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