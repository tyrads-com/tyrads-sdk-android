package com.tyrads.sdk.acmo.core

import AcmoKeyNames
import android.content.Context
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OnboardingCheck {
    suspend fun checkOnboardingStatus(context: Context): Boolean = withContext(Dispatchers.Main) {
        val tyrads = Tyrads.getInstance()

        // Check if Tyrads is initialized
        if (tyrads.publisherUserID == null) {
            return@withContext false
        }

        val preferences = tyrads.preferences
        val publisherUserID = tyrads.publisherUserID!!

        // Check privacy acceptance for THIS specific user
        val privacyAccepted = preferences.getBoolean(
            AcmoKeyNames.PRIVACY_ACCEPTED_FOR_USER_ID + publisherUserID,
            false
        )

        if (!privacyAccepted) {
            // Navigate to privacy policy page using showOffers which opens AcmoApp
            tyrads.showOffers()
            return@withContext false
        }

        // Check usage stats permission (Android only)
        try {
            val usageStatsPermission = checkUsageStatsPermission(context)

            if (!usageStatsPermission) {
                // Navigate to usage permissions page
                try {
                    tyrads.navController.navigate("usage-permissions")
                } catch (e: Exception) {
                    tyrads.showOffers()
                }
                return@withContext false
            }
        } catch (e: Exception) {
            // If there's an error checking permissions, continue
            tyrads.log("Error checking usage stats permission: ${e.message}")
        }

        // Check if this is a new user who needs to update profile
        if (tyrads.newUser) {
            try {
                tyrads.navController.navigate("users-update")
            } catch (e: Exception) {
                tyrads.showOffers()
            }
            return@withContext false
        }

        return@withContext true
    }

      private fun checkUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}