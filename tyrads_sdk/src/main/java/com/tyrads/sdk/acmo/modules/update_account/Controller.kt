package com.tyrads.sdk.acmo.modules.update_account

import android.util.Log
import androidx.annotation.Keep
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.TyradsUpdateUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Keep
class AcmoUpdateUserAccountController {

    private val repository = AcmoUpdateUserAccountRepository()

    suspend fun updateUserAccount(userUpdateInfo: TyradsUpdateUserInfo): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val isSuccess =  repository.updateUserAccount(userUpdateInfo)
                isSuccess
            } catch (e: Exception) {
                Tyrads.getInstance().log(
                    "AcmoUpdateUserAccountController: Failed to update account - ${e.message}",
                    Log.ERROR
                )
                false
            }
        }
    }
}
