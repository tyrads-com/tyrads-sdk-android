import androidx.annotation.Keep

@Keep
object AcmoEndpointNames {
    const val INITIALIZE = "initialize"
    const val OFFERS = "campaigns"
    const val ACTIVE_OFFERS = "campaigns/activated"
    const val UPDATE_USER = "update-user"
    const val DEVICE_DETAILS = "user-device"
    const val OFFER_SUMMARY = "campaigns/activated/summary"
    const val ENGAGEMENT = "account/engagement"
    const val USAGE_STATS = "usage-stats"
    const val USER_ACTIVITIES = "account/activity"
    const val CHECK_PROFILE_COMPLETION = "check-profile-completion"
}