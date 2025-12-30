package com.tyrads.sdk.acmo.modules.input_models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AcmoInitModel(
    @SerializedName("data")
    val data: InitData
)
@Keep
data class InitData(
    @SerializedName("newRegisteredUser")
    val newRegisteredUser: Boolean = false,
    @SerializedName("user")
    val user: User,
    @SerializedName("publisherApp")
    val publisherApp: PublisherApp,
    val token: String
)
@Keep
data class User(
    @SerializedName("publisherUserId")
    val publisherUserId: String
)
@Keep
data class PublisherApp(
    @SerializedName("headerColor")
    val headerColor: String = "",
    @SerializedName("mainColor")
    val mainColor: String = "",
    @SerializedName("premiumColor")
    val premiumColor: String = ""
)

