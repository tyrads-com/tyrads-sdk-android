package com.tyrads.sdk.acmo.modules.input_models

data class TyradsUpdateUserInfo(
    /**
     * Can be used to identify the user to prevent fraud.
     * Format: email address
     */
    val email: String? = null,

    /**
     * Can be used to identify the user to prevent fraud.
     * Format: + should be replaced with 00, example: 0015555551234 for a US number
     */
    val phoneNumber: String? = null,

    /** Age of the user, used for targeting. */
    val age: Int? = null,

    /**
     * Gender of the user, used for targeting.
     * 1 = male, 2 = female
     */
    val gender: Int? = null,

    /** Custom field for storing additional data. */
    val sub2: String? = null,

    /** Custom field for storing additional data. */
    val sub3: String? = null,

    /** Custom field for storing additional data. */
    val sub4: String? = null,
)
