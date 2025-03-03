package com.tyrads.sdk.acmo.modules.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tyrads.sdk.R
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.modules.dashboard.components.MyGamesButton
import com.tyrads.sdk.acmo.modules.dashboard.components.PremiumHeaderSection
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.*
import com.tyrads.sdk.ui.theme.*

@Composable
fun GameOffersScreen(
    data: List<BannerData>
) {
    data.forEachIndexed { index, game ->
        GameOfferItem(game = game, rank = index + 1)
    }
}

@Composable
fun GameOfferItem(game: BannerData, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = gameListVerticalPadding)
            .clickable {
                Tyrads.getInstance().showOffers(route = "campaign-details", campaignID = game.campaignId)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(game.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = game.title,
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(imageCornerRadius))
            )
            Spacer(modifier = Modifier.width(gameInfoSpacerWidth))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = gameTextFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(gameListSpacerHeight))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = gameListTopVerticalPadding)
                ) {
                    Surface(
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(playButtonCornerRadius)
                    ) {
                        Text(
                            text = stringResource(id= R.string.dashboard_top_ranking, rank),
                            color = Color.White,
                            fontSize = gameListTopRankFontSize,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(
                                horizontal = gameListTopRankHorizontalPadding,
                                vertical = gameListTopRankVerticalPadding
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(gameListSpacerWidth6))
                    Image(
                        painter = painterResource(id = R.drawable.ic_coin2),
                        contentDescription = "Coin Icon",
                        modifier = Modifier.size(coinIconSize)
                    )
                    Spacer(modifier = Modifier.width(gameListSpacerWidth4))
                    Text(
                        text = game.points.numeral(),
                        color = Color.Gray,
                        fontSize = pointsFontSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(gameListSpacerWidth8))
        Button(
            onClick = { },
            shape = RoundedCornerShape(gameListButtonCornerShape),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            contentPadding = PaddingValues(
                horizontal = gameListButtonPaddingHorizontal,
                vertical = gameListButtonPaddingVertical
            ),
            modifier = Modifier.height(gameListButtonHeight)
        ) {
            Text(
                text = stringResource(R.string.dashboard_play_button),
                fontSize = gameListButtonFontSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
