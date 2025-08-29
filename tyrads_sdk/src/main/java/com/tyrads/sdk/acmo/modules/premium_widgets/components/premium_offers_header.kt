package com.tyrads.sdk.acmo.modules.premium_widgets.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.core.services.LocalizationService
import com.tyrads.sdk.acmo.modules.input_models.headerIconSpacing
import com.tyrads.sdk.acmo.modules.input_models.headerTextSpacing
import kotlinx.coroutines.launch

@Composable
fun PremiumHeaderSection(
    modifier: Modifier = Modifier,
    localizationService: LocalizationService

) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.diamond),
                contentDescription = "Diamond",
                modifier = Modifier
                    .size(14.dp),
                tint = Tyrads.getInstance().premiumColor.toColor()
            )
            Spacer(modifier = Modifier.width(headerTextSpacing))
            Text(
                text = localizationService.translate("data.widget.page.title"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Tyrads.getInstance().premiumColor.toColor()
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                coroutineScope.launch {
                    Tyrads.getInstance().showOffers()
                }
            }
        ) {
            Text(
                text = localizationService.translate("data.widget.button.moreOffers"),
                color = Tyrads.getInstance().premiumColor.toColor(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(headerIconSpacing))
            Icon(
                painter = painterResource(id = R.drawable.angle_up),
                contentDescription = "Arrow",
                modifier = Modifier
                    .size(14.dp)
                    .rotate(degrees = 90f),
                tint = Tyrads.getInstance().premiumColor.toColor()
            )
        }

    }
}