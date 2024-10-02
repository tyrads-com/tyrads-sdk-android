package com.tyrads.sdk

data class TyradsUserInfo(
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

    /**
     * Can be used to segment the user base in different groups
     * allowing you to reward them a higher or lower % based on this group.
     * Example: "High purchasing user"
     */
    val userGroup: String? = null
)
