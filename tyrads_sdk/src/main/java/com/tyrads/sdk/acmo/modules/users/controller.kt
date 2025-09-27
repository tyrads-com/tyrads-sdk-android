package com.tyrads.sdk.acmo.modules.users

import TyradsActivity
import com.tyrads.sdk.Tyrads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcmoUsersController {

    private val repository = AcmoUsersRepository()
    var submitting: Boolean = false

    suspend fun updateUser(userId: String, age: Int?, gender: Int?) = withContext(Dispatchers.Default) {
        try {
            val tyradsInstance = Tyrads.getInstance()

            val fd = mutableMapOf<String, Any>()
            age?.let { fd["age"] = it }
            gender?.let { fd["gender"] = it }

            // Delegate to the repository for the network call
            repository.updateUser(userId, fd)

            tyradsInstance.track(TyradsActivity.profileUpdated)
            tyradsInstance.newUser = false
            tyradsInstance.log("User updated successfully via AcmoUsersController")

        } catch (e: Exception) {
            Tyrads.getInstance().log("Exception in AcmoUsersController.updateUser: ${e.message}", android.util.Log.ERROR)
            throw e
        }
    }
}