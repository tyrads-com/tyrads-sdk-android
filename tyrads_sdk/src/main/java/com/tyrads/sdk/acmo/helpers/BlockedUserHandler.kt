package com.tyrads.sdk.acmo.helpers

import android.util.Base64
import android.util.Log
import com.google.gson.JsonParser
import AcmoConfig
import com.tyrads.sdk.Tyrads

object BlockedUserHandler {

    fun handle(responseBody: String, userId: String): Boolean {
        return try {
            val json = JsonParser.parseString(responseBody).asJsonObject
            val data = json.getAsJsonObject("data") ?: return false
            val token = data.get("token")?.asString ?: return false

            val instance = Tyrads.getInstance()
            instance.isBlocked = true
            instance.token = token
            instance.publisherUserID = userId
            instance.newUser = false

            try {
                extractColorsFromToken(token, instance)
            } catch (e: Exception) {
                Log.e(AcmoConfig.TAG, "BlockedUserHandler: failed to extract colors from token: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e(AcmoConfig.TAG, "BlockedUserHandler: failed to handle blocked response: ${e.message}")
            false
        }
    }

    private fun extractColorsFromToken(token: String, instance: Tyrads) {
        val parts = token.split(".")
        if (parts.size < 2) return

        val payload = parts[1]
        val paddedPayload = payload.padEnd((payload.length + 3) / 4 * 4, '=')
        val decodedBytes = Base64.decode(paddedPayload, Base64.URL_SAFE or Base64.NO_WRAP)
        val decodedString = String(decodedBytes, Charsets.UTF_8)

        val payloadJson = JsonParser.parseString(decodedString).asJsonObject
        val publisherApp = payloadJson.getAsJsonObject("data")
            ?.getAsJsonObject("publisherApp") ?: return

        publisherApp.get("mainColor")?.asString?.takeIf { it.isNotBlank() }?.let {
            instance.mainColor = it
        }
        publisherApp.get("headerColor")?.asString?.takeIf { it.isNotBlank() }?.let {
            instance.headerColor = it
        }
        publisherApp.get("premiumColor")?.asString?.takeIf { it.isNotBlank() }?.let {
            instance.premiumColor = it
        }
    }
}

