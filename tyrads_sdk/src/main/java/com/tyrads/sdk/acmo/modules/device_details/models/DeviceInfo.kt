import android.app.ActivityManager
import android.content.Context
import android.os.Build

data class DeviceInfo(
    val board: String,
    val bootloader: String,
    val brand: String,
    val device: String,
    val display: String,
    val fingerprint: String,
    val hardware: String,
    val host: String,
    val id: String,
    val manufacturer: String,
    val model: String,
    val product: String,
    val supported32BitAbis: List<String>,
    val supported64BitAbis: List<String>,
    val supportedAbis: List<String>,
    val tags: String,
    val type: String,
    val version: VersionInfo,
    val isLowRamDevice: Boolean,
    val serialNumber: String
)

data class VersionInfo(
    val baseOS: String? = null,
    val previewSdkInt: Int? = null,
    val securityPatch: String? = null,
    val codename: String,
    val incremental: String,
    val release: String,
    val sdkInt: Int
)
