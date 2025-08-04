package com.tyrads.sdk.acmo.modules.dashboard

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.Tyrads.PremiumWidgetStyles

class TopPremiumOffersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var _showMore: Boolean = true
    private var _showMyOffers: Boolean = false
    private var _showMyOffersEmptyView: Boolean = false
    private var _style: PremiumWidgetStyles = PremiumWidgetStyles.LIST

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.TopPremiumOffersView,
                0, 0
            ).apply {
                try {
                    _showMore = getBoolean(R.styleable.TopPremiumOffersView_showMore, true)
                    _showMyOffers = getBoolean(R.styleable.TopPremiumOffersView_showMyOffers, false)
                    _showMyOffersEmptyView = getBoolean(R.styleable.TopPremiumOffersView_showMyOffersEmptyView, false)
                    val styleOrdinal = getInt(
                        R.styleable.TopPremiumOffersView_style,
                        PremiumWidgetStyles.LIST.ordinal
                    )
                    _style = PremiumWidgetStyles.entries.toTypedArray().getOrElse(styleOrdinal) { PremiumWidgetStyles.LIST }
                } finally {
                    recycle()
                }
            }
        }
    }

    fun setShowMore(value: Boolean) {
        _showMore = value
        invalidate()
    }

    fun setShowMyOffers(value: Boolean) {
        _showMyOffers = value
        invalidate()
    }

    fun setShowMyOffersEmptyView(value: Boolean) {
        _showMyOffersEmptyView = value
        invalidate()
    }

    fun setStyle(value: PremiumWidgetStyles) {
        _style = value
        invalidate()
    }

    @JvmOverloads
    fun setConfig(
        showMore: Boolean = this._showMore,
        showMyOffers: Boolean = this._showMyOffers,
        showMyOffersEmptyView: Boolean = this._showMyOffersEmptyView,
        style: PremiumWidgetStyles = this._style
    ) {
        _showMore = showMore
        _showMyOffers = showMyOffers
        _showMyOffersEmptyView = showMyOffersEmptyView
        _style = style
        invalidate()
    }

    @Composable
    override fun Content() {
        Tyrads.getInstance().TopPremiumOffers(
            showMore = _showMore,
            showMyOffers = _showMyOffers,
            showMyOffersEmptyView = _showMyOffersEmptyView,
            widgetStyle = _style
        )
    }
}
