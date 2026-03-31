package com.tyrads.sdk.acmo.modules.input_models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AcmoInitModel(
    @SerializedName("data")
    val data: Data
)

@Keep
data class Data(
    @SerializedName("newRegisteredUser")
    val newRegisteredUser: Boolean = false,
    @SerializedName("newRegisteredDevice")
    val newRegisteredDevice: Boolean = false,
    @SerializedName("accountInfo")
    val accountInfo: AccountInfo,
    @SerializedName("appInfo")
    val appInfo: AppInfo,
    val token: String
)

@Keep
data class AccountInfo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("publisherUserId")
    val publisherUserId: String
)

@Keep
data class AppInfo(
    @SerializedName("headerColor")
    val headerColor: String = "",
    @SerializedName("mainColor")
    val mainColor: String = "",
    @SerializedName("premiumColor")
    val premiumColor: String = ""
)
