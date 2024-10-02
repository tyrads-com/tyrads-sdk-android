package com.tyrads.sdk

/**
 * Represents information about the media source for user acquisition.
 */
data class TyradsMediaSourceInfo(
    /** The name of the source where the user was acquired. */
    val mediaSourceName: String? = null,

    /** The name of the campaign used to acquire the user. */
    val mediaCampaignName: String? = null,

    /** The ID of the source where the user was acquired. */
    val mediaSourceId: String? = null,

    /** The sub-source ID of where the user was acquired. */
    val mediaSubSourceId: String? = null,

    /** Indicates if the user acquisition was done via incentivized channels. */
    val incentivized: Boolean? = null,

    /** The name of the ad set used to acquire the user. */
    val mediaAdsetName: String? = null,

    /** The ID of the ad set used to acquire the user. */
    val mediaAdsetId: String? = null,

    /** The name of the creative used to acquire the user. */
    val mediaCreativeName: String? = null,

    /** The ID of the creative used to acquire the user. */
    val mediaCreativeId: String? = null,

    /** Custom field for storing additional data. Not received on postback. */
    val sub1: String? = null,

    /** Custom field for storing additional data. Not received on postback. */
    val sub2: String? = null,

    /** Custom field for storing additional data. Can be sent back on postback. */
    val sub3: String? = null,

    /** Custom field for storing additional data. Can be sent back on postback. */
    val sub4: String? = null,

    /** Custom field for storing additional data. Not received on postback. */
    val sub5: String? = null
)
