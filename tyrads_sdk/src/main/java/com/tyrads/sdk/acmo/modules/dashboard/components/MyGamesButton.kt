package com.tyrads.sdk.acmo.modules.dashboard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.modules.input_models.myGamesButtonCornerRadius
import com.tyrads.sdk.acmo.modules.input_models.myGamesButtonFontSize
import com.tyrads.sdk.acmo.modules.input_models.myGamesButtonHeight
import com.tyrads.sdk.acmo.modules.input_models.myGamesButtonPadding

@Composable
fun MyGamesButton() {
    Button(
        onClick = {
            Tyrads.getInstance().showOffers()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = myGamesButtonPadding,
                end = myGamesButtonPadding,
//                top =  myGamesButtonPadding,
                bottom = myGamesButtonPadding
            )
            .height(myGamesButtonHeight),
        colors = ButtonDefaults.buttonColors(containerColor = Tyrads.getInstance().premiumColor.toColor()),
        shape = RoundedCornerShape(myGamesButtonCornerRadius)
    ) {
        Text(
            text = stringResource(id = R.string.dashboard_my_games),
            fontSize = myGamesButtonFontSize,
            fontWeight = FontWeight.Bold
        )
    }
}