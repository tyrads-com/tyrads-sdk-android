import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.concurrent.TimeUnit

class AcmoUsageStatsController() {
    private val repository = AcmoUsageStatRepository()

     @Composable
     fun CheckUsageStats(packageName: String? = null) {

        val status =
            checkUsagePermission() ?: false


        if (!status) {
            Tyrads.getInstance().Dialog {
                AcmoUsageStatsDialog(
                    dismissible = false,
                    onDismissRequest = {
                        CoroutineScope(Dispatchers.IO).launch {
                            saveUsageStats(packageName)
                        }
                    }
                )
            }
        }
    }


    fun isUsagePermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager?
        val mode = appOps!!.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        if (mode == AppOpsManager.MODE_ALLOWED) {
            Log.d("UTILS", "Usage permission is granted")
            return true
        }
        Log.d("UTILS", "Usage permission is not granted")
        return false
    }

    fun grantUsagePermission() {
        val context = Tyrads.getInstance().context
        if (!isUsagePermission(context)) {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = Uri.parse("package:" + context.packageName);
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }
    internal fun checkUsagePermission(): Boolean {
        val appOps = getSystemService(
            Tyrads.getInstance().context,
            AppOpsManager::class.java
        ) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            Tyrads.getInstance().context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun saveUsageStats(
        packageName: String? = null,
        isForeground: Boolean = false,
        days: Int = 150,
        getPackageInfo: Boolean = false, // Unused parameter
        getDeviceInfo: Boolean = false, // Unused parameter
        saveUsage: Boolean = true
    ): List<Map<String, Any>> {
        return try {

            val endDate = System.currentTimeMillis()
            val startDate = endDate - (days * 24 * 60 * 60 * 1000L)
            val temp = mutableListOf<Map<String, Any>>()

            val aggregateUsageStats = UsageStats.queryAndAggregateUsageStats(
                Tyrads.getInstance().context,
                startDate,
                endDate
            )

            for ((name, value) in aggregateUsageStats) {
                if (packageName == null || packageName == value["packageName"]) {
                    val foregroundTime = if (!isForeground) {
                        value["totalTimeInForeground"]?.toLongOrNull() ?: 0L
                    } else {
                        (value["totalTimeInForeground"]?.toLongOrNull() ?: 0L) +
                                (System.currentTimeMillis() - (value["lastTimeStamp"]?.toLongOrNull()
                                    ?: 0L))
                    }

                    val item = mapOf(
                        "packageName" to (value["packageName"] ?: ""),
                        "firstTimeStamp" to (value["firstTimeStamp"]?.toLongOrNull() ?: 0L),
                        "lastTimeStamp" to (value["lastTimeStamp"]?.toLongOrNull() ?: 0L),
                        "lastTimeUsed" to (value["lastTimeUsed"]?.toLongOrNull() ?: 0L),
                        "foregroundTime" to TimeUnit.MILLISECONDS.toSeconds(foregroundTime)
                    )
                    temp.add(item)
                }
            }


            if (temp.isNotEmpty() && saveUsage) {
                repository.saveUsageStats(mapOf("usageStatsList" to temp))
            }

            temp
        } catch (e: Exception) {
            Log.d("TyradsSdk", "Error: $e")
            emptyList()
        }
    }

    fun getDeviceAgeTime(): Long? {
        return try {
            val pm = Tyrads.getInstance().context.packageManager
            val timestamp = Date().time
            val pkgs = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            val installTimeCount = mutableMapOf<Long, Int>()
            var highestCount = 1

            pkgs?.forEach { packageInfo ->
                val firstInstallTime = packageInfo.firstInstallTime
                if (firstInstallTime > 1293840000000) { // After Gingerbread
                    installTimeCount[firstInstallTime] =
                        installTimeCount.getOrDefault(firstInstallTime, 0) + 1
                    highestCount = maxOf(highestCount, installTimeCount[firstInstallTime]!!)
                }
            }

            val filteredMap = installTimeCount.filterValues { it == highestCount }
            installTimeCount.clear()
            installTimeCount.putAll(filteredMap)

            installTimeCount.minByOrNull { it.key }?.key
        } catch (e: Exception) {
            null
        }
    }

}