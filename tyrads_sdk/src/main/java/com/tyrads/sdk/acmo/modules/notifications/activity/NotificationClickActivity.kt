package com.tyrads.sdk.acmo.modules.notifications.activity//package com.tyrads.sdk.acmo.modules.notifications.activity
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import androidx.activity.ComponentActivity
//import com.tyrads.sdk.Tyrads
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class NotificationClickActivity : ComponentActivity() {
//
//    companion object {
//        private const val TAG = "NotificationClick"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val deepLink = intent.getStringExtra("deepLink")
//        Log.d(TAG, "NotificationClickActivity started with deepLink: $deepLink")
//
//        if (!deepLink.isNullOrEmpty()) {
//            // Open main app first
//            openMainApp()
//
//            // Wait 3 seconds then navigate to offers
//            Handler(Looper.getMainLooper()).postDelayed({
//                CoroutineScope(Dispatchers.Main).launch {
//                    try {
//                        Log.d(TAG, "Navigating to offers with route: $deepLink")
//                        Tyrads.getInstance().showOffers(route = deepLink)
//                        finish() // Finish AFTER navigation
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Failed to open deep link: ${e.message}", e)
//                        finish()
//                    }
//                }
//            }, 5000) // 5 second delay
//        } else {
//            finish()
//        }
//    }
//
//    private fun openMainApp() {
//        try {
//            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
//            if (launchIntent != null) {
//                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                startActivity(launchIntent)
//                Log.d(TAG, "Main app opened")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to open main app: ${e.message}", e)
//        }
//    }
//}