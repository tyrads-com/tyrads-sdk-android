package com.tyrads.sdk.acmo.modules.users

import AcmoEndpointNames
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.helpers.AcmoEncrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcmoUsersRepository {

    suspend fun updateUser(userId: String, fd: MutableMap<String, Any>) = withContext(Dispatchers.IO) {
        try {
            val tyradsInstance = Tyrads.getInstance()
            val encKey = tyradsInstance.encKey // Get encryption key from Tyrads instance

            val encData = if (tyradsInstance.isSecure && encKey != null) {
                AcmoEncrypt(encryptionKey = encKey).encryptDataAESGCM(data = fd)
            } else {
                fd
            }

            val (request, response, result) = Fuel.put(AcmoEndpointNames.UPDATE_USER)
                .body(Gson().toJson(if (tyradsInstance.isSecure) encData else fd))
                .response()

            when (result) {
                is Result.Success -> {
                    tyradsInstance.log("User updated successfully in repository")
                    // No return value needed, just successful completion
                }
                is Result.Failure -> {
                    val error = result.getException()
                    val errorMessage = String(response.data)
                    tyradsInstance.log("User update failed in repository: ${error.message}", android.util.Log.ERROR)
                    tyradsInstance.log("Server Message: $errorMessage", android.util.Log.ERROR)
                    throw Exception("Failed to update user: $errorMessage")
                }
            }
        } catch (e: Exception) {
            Tyrads.getInstance().log("Exception in AcmoUsersRepository.updateUser: ${e.message}", android.util.Log.ERROR)
            throw e
        }
    }
}