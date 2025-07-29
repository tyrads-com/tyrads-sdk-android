package com.tyrads.sdk.acmo.helpers

import android.content.Context
import androidx.annotation.Keep
import com.tyrads.sdk.acmo.modules.dashboard.TopPremiumOffersView

@Keep
object TyradsViewHelper {

    @JvmStatic
    fun createTopPremiumOffersView(context: Context): TopPremiumOffersView {
        return TopPremiumOffersView(context)
    }

    @JvmStatic
    fun createTopPremiumOffersView(
        context: Context,
        showMore: Boolean,
        showMyOffers: Boolean,
        showMyOffersEmptyView: Boolean,
        style: Int
    ): TopPremiumOffersView {
        return TopPremiumOffersView(context).apply {
            setConfig(showMore, showMyOffers, showMyOffersEmptyView, style)
        }
    }
}