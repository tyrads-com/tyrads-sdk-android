package com.tyrads.sdk.acmo.modules.update_account

import AcmoEndpointNames
import androidx.annotation.Keep
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.helpers.AcmoEncrypt
import com.tyrads.sdk.acmo.modules.input_models.TyradsUpdateUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Keep
class AcmoUpdateUserAccountRepository {

    suspend fun updateUserAccount(userUpdateInfo: TyradsUpdateUserInfo): Boolean =
        withContext(Dispatchers.IO) {
            val tyrads = Tyrads.getInstance()

            val fd = mutableMapOf<String, Any>()
            userUpdateInfo.email?.let { fd["email"] = it }
            userUpdateInfo.phoneNumber?.let { fd["phoneNumber"] = it }
            userUpdateInfo.age?.let { fd["age"] = it }
            userUpdateInfo.gender?.let { fd["gender"] = it }
//            userUpdateInfo.sub1?.let { fd["sub1"] = it }
            userUpdateInfo.sub2?.let { fd["sub2"] = it }
            userUpdateInfo.sub3?.let { fd["sub3"] = it }
            userUpdateInfo.sub4?.let { fd["sub4"] = it }
//            userUpdateInfo.sub5?.let { fd["sub5"] = it }

            val currentEncKey = tyrads.encKey
            val encData =
                if (tyrads.isSecure && !currentEncKey.isNullOrBlank()) {
                    AcmoEncrypt(encryptionKey = currentEncKey).encryptDataAESGCM(data = fd)
                } else {
                    emptyMap()
                }

            val body = Gson().toJson(
                if (tyrads.isSecure && !currentEncKey.isNullOrBlank()) encData else fd
            )

            val (_,_, result) = Fuel.put(AcmoEndpointNames.ACCOUNT)
                .body(body)
                .response()

            when (result) {
                is Result.Success -> true
                is Result.Failure -> throw result.error
            }
        }
}
