package com.tyrads.sdk.acmo.modules.dashboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.headerFontSize
import com.tyrads.sdk.acmo.modules.input_models.headerIconSpacing
import com.tyrads.sdk.acmo.modules.input_models.headerPaddingBottom
import com.tyrads.sdk.acmo.modules.input_models.headerPaddingEnd
import com.tyrads.sdk.acmo.modules.input_models.headerPaddingStart
import com.tyrads.sdk.acmo.modules.input_models.headerPaddingTop
import com.tyrads.sdk.acmo.modules.input_models.headerTextSpacing
import com.tyrads.sdk.acmo.modules.input_models.moreOffersFontSize
import com.tyrads.sdk.acmo.modules.input_models.moreOffersIconSize
import com.tyrads.sdk.acmo.modules.input_models.starIconSize
import com.tyrads.sdk.ui.theme.PrimaryBlue

@Composable
fun PremiumHeaderSection(
    showMore: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = headerPaddingStart,
                end = headerPaddingEnd,
                top = headerPaddingTop,
                bottom = headerPaddingBottom
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_star_new),
                contentDescription = "Star",
                modifier = Modifier.size(starIconSize)
            )
            Spacer(modifier = Modifier.width(headerTextSpacing))
            Text(
                text = stringResource(id = R.string.dashboard_suggested_offers),
                fontSize = headerFontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if(showMore) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    Tyrads.getInstance().showOffers()
                }
            ) {
                Text(
                    text = stringResource(id = R.string.dashboard_more_offers),
                    color = PrimaryBlue,
                    fontSize = moreOffersFontSize,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(headerIconSpacing))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Arrow",
                    modifier = Modifier.size(moreOffersIconSize),
                    tint = PrimaryBlue
                )
            }
        }
    }
}