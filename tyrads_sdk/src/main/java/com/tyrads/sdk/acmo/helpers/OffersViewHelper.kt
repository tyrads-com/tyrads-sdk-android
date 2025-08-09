package com.tyrads.sdk.acmo.helpers

import android.content.Context
import androidx.annotation.Keep
import com.tyrads.sdk.Tyrads.PremiumWidgetStyles
import com.tyrads.sdk.acmo.modules.premium_widgets.TopPremiumOffersView

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
        val enumStyle = PremiumWidgetStyles.entries.toTypedArray().getOrElse(style) {
            PremiumWidgetStyles.LIST
        }

        return TopPremiumOffersView(context).apply {
            setConfig(
                showMore = showMore,
                showMyOffers = showMyOffers,
                showMyOffersEmptyView = showMyOffersEmptyView,
                style = enumStyle
            )
        }
    }
}
