package com.tyrads.sdk.acmo.modules.dashboard

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads

class TopPremiumOffersView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var _showMore: Boolean = true
    private var _showMyOffers: Boolean = false
    private var _showMyOffersEmptyView: Boolean = false
    private var _style: Int = 2

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
                    _style = getInt(R.styleable.TopPremiumOffersView_style, 2)
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

    fun setStyle(value: Int) {
        _style = value
        invalidate()
    }

    @Composable
    override fun Content() {
        Tyrads.getInstance().TopPremiumOffers(
            showMore = _showMore,
            showMyOffers = _showMyOffers,
            showMyOffersEmptyView = _showMyOffersEmptyView,
            style = _style
        )
    }
}
