package com.tyrads.sdk.acmo.modules.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.numeral
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.coinIconSize
import com.tyrads.sdk.acmo.modules.input_models.gameInfoSpacerWidth
import com.tyrads.sdk.acmo.modules.input_models.gameListButtonCornerShape
import com.tyrads.sdk.acmo.modules.input_models.gameListButtonFontSize
import com.tyrads.sdk.acmo.modules.input_models.gameListButtonHeight
import com.tyrads.sdk.acmo.modules.input_models.gameListButtonPaddingHorizontal
import com.tyrads.sdk.acmo.modules.input_models.gameListButtonPaddingVertical
import com.tyrads.sdk.acmo.modules.input_models.gameListSpacerHeight
import com.tyrads.sdk.acmo.modules.input_models.gameListSpacerWidth4
import com.tyrads.sdk.acmo.modules.input_models.gameListSpacerWidth6
import com.tyrads.sdk.acmo.modules.input_models.gameListSpacerWidth8
import com.tyrads.sdk.acmo.modules.input_models.gameListTopRankFontSize
import com.tyrads.sdk.acmo.modules.input_models.gameListTopRankHorizontalPadding
import com.tyrads.sdk.acmo.modules.input_models.gameListTopRankVerticalPadding
import com.tyrads.sdk.acmo.modules.input_models.gameListTopVerticalPadding
import com.tyrads.sdk.acmo.modules.input_models.gameListVerticalPadding
import com.tyrads.sdk.acmo.modules.input_models.gameTextFontSize
import com.tyrads.sdk.acmo.modules.input_models.imageCornerRadius
import com.tyrads.sdk.acmo.modules.input_models.imageSize
import com.tyrads.sdk.acmo.modules.input_models.playButtonCornerRadius
import com.tyrads.sdk.acmo.modules.input_models.pointsFontSize
import com.tyrads.sdk.acmo.modules.input_models.rewardsFontSize
import com.tyrads.sdk.ui.theme.GrayColor

@Composable
fun GameOffersScreen(
    data: List<BannerData>
) {
    data.forEachIndexed { index, game ->
        Box(modifier = Modifier.padding(horizontal = 10.dp)) {
            GameOfferItem(game = game, rank = index + 1)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameOfferItem(game: BannerData, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = gameListVerticalPadding)
            .clickable {
                Tyrads.getInstance()
                    .showOffers(route = "campaign-details", campaignID = game.campaignId)
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
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
                Spacer(modifier = Modifier.height(gameListSpacerHeight))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = gameListTopVerticalPadding)
                ) {
                    Box(
                        modifier = Modifier.height(18.dp)
                            .clip(RoundedCornerShape(playButtonCornerRadius),)
                            .background(Tyrads.getInstance().premiumColor.toColor())
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = stringResource(id= R.string.dashboard_top_ranking, rank),
                            color = Color.White,
                            fontSize = gameListTopRankFontSize,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(
                                horizontal = gameListTopRankHorizontalPadding,
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(gameListSpacerWidth6))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(game.currency.adUnitCurrencyIcon)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Game Icon",
                        modifier = Modifier
                            .size(coinIconSize)
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
                    Text(
                        text = "  ${game.rewards} ${pluralStringResource(R.plurals.offers_rewards, game.rewards)}",
                        color = GrayColor,
                        fontSize = rewardsFontSize,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(gameListSpacerWidth8))
        Button(
            onClick = {Tyrads.getInstance()
                .showOffers(route = "campaign-details", campaignID = game.campaignId) },
            shape = RoundedCornerShape(gameListButtonCornerShape),
            colors = ButtonDefaults.buttonColors(containerColor = Tyrads.getInstance().premiumColor.toColor()),
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
