package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class InAppDialogType {
    LIMITED_TIME,
    CURRENCY_SALE
}

object InAppNotificationManager {

    private const val PREFS_NAME = "in_app_notification_prefs"
    private const val KEY_SHOWN_LIMITED_TIME = "isShownInAppNotification_limitedTime_"
    private const val KEY_SHOWN_CURRENCY_SALE = "isShownInAppNotification_currencySale_"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val limitedTimeVisible = MutableStateFlow(false)
    private val currencySaleVisible = MutableStateFlow(false)

    private var sharedPreferences: SharedPreferences? = null

    val activeDialog: StateFlow<InAppDialogType?> =
        combine(
            limitedTimeVisible,
            currencySaleVisible
        ) { limited, currency ->
            when {
                limited -> InAppDialogType.LIMITED_TIME
                currency -> InAppDialogType.CURRENCY_SALE
                else -> null
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        if (sharedPreferences == null) {
            sharedPreferences = Tyrads.getInstance().preferences
        }
    }

    private fun getUserSpecificKey(baseKey: String): String {
        val userId = Tyrads.getInstance().publisherUserID ?: "default"
        return "$baseKey$userId"
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun hasShownLimitedTime(): Boolean {
        val key = getUserSpecificKey(KEY_SHOWN_LIMITED_TIME)
        val stored = try {
            sharedPreferences?.getString(key, null)
        } catch (e: ClassCastException) {
            null
        }
        return stored == today()
    }

    private fun hasShownCurrencySale(): Boolean {
        val key = getUserSpecificKey(KEY_SHOWN_CURRENCY_SALE)
        val stored = try {
            sharedPreferences?.getString(key, null)
        } catch (e: ClassCastException) {
            null
        }
        return stored == today()
    }

    private fun markLimitedTimeAsShown() {
        val key = getUserSpecificKey(KEY_SHOWN_LIMITED_TIME)
        sharedPreferences?.edit()?.putString(key, today())?.apply()
    }

    private fun markCurrencySaleAsShown() {
        val key = getUserSpecificKey(KEY_SHOWN_CURRENCY_SALE)
        sharedPreferences?.edit()?.putString(key, today())?.apply()
    }

    fun setLimitedTimeVisible(visible: Boolean, hasEvents: Boolean = true) {
        if (sharedPreferences == null) {
            Log.d("Check 1", "11" )
            limitedTimeVisible.value = false
            return
        }

        if (hasShownLimitedTime()) {
            limitedTimeVisible.value = false
            return
        }

        if (visible && !hasEvents) {
            markLimitedTimeAsShown()
            limitedTimeVisible.value = false
            return
        }

        if (visible && hasEvents) {
            limitedTimeVisible.value = true
            markLimitedTimeAsShown()
        } else {
            limitedTimeVisible.value = false
        }
    }

    fun setCurrencySaleVisible(visible: Boolean) {
        if (sharedPreferences == null) {
            currencySaleVisible.value = false
            return
        }

        if (hasShownCurrencySale()) {
            currencySaleVisible.value = false
            return
        }

        if (visible) {
            currencySaleVisible.value = true
            markCurrencySaleAsShown()
        } else {
            currencySaleVisible.value = false
        }
    }

    fun dismiss(type: InAppDialogType) {
        when (type) {
            InAppDialogType.LIMITED_TIME ->
                limitedTimeVisible.value = false

            InAppDialogType.CURRENCY_SALE ->
                currencySaleVisible.value = false
        }
    }

    fun reset() {
        limitedTimeVisible.value = false
        currencySaleVisible.value = false
    }

    fun resetPreferences() {
        val limitedKey = getUserSpecificKey(KEY_SHOWN_LIMITED_TIME)
        val currencyKey = getUserSpecificKey(KEY_SHOWN_CURRENCY_SALE)
        sharedPreferences?.edit()?.apply {
            remove(limitedKey)
            remove(currencyKey)
            apply()
        }
    }

    fun resetAllUsersPreferences() {
        sharedPreferences?.edit()?.clear()?.apply()
    }

    fun getNotificationState(): Pair<Boolean, Boolean> {
        return Pair(hasShownLimitedTime(), hasShownCurrencySale())
    }

    fun getCurrentUserId(): String {
        return Tyrads.getInstance().publisherUserID ?: "default"
    }
}