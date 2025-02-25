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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tyrads.sdk.R
import com.tyrads.sdk.NetworkCommons
import com.tyrads.sdk.acmo.modules.input_models.BannerData
import com.tyrads.sdk.acmo.modules.input_models.*
import com.tyrads.sdk.ui.theme.*


class PremiumActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameOffersScreen()
        }
    }
}

@Composable
fun GameOffersScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightGrayColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = offersScreenPaddingTop),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameOffersCard()
        }
    }
}

@Composable
fun GameOffersCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                horizontal = cardPaddingHorizontal,
                vertical = cardPaddingVertical
            ),
        shape = RoundedCornerShape(
            topStart = cardCornerTopStart,
            topEnd = cardCornerTopEnd,
            bottomEnd = cardCornerBottomEnd,
            bottomStart = cardCornerBottomStart
        ),
        colors = CardDefaults.cardColors(containerColor = WhiteColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = cardPaddingHorizontal, vertical = cardPaddingVertical)
        ) {
            HeaderSection()
            Spacer(modifier = Modifier.height(headerTextSpacing))
            GameList()
            Spacer(modifier = Modifier.height(cardGameListSpacing))
            MyGamesButton()
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = headerPaddingEnd),
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
                text = "Suggested Offers",
                fontSize = headerFontSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { }
        ) {
            Text(
                text = "More Offers",
                color = PrimaryBlue,
                fontSize = moreOffersFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

@Composable
fun GameList() {
    var games by remember { mutableStateOf<List<BannerData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val networkCommons = remember { NetworkCommons() }

    LaunchedEffect(Unit) {
        networkCommons.fetchCampaigns(
            onSuccess = {
                games = it
                isLoading = false
            },
            onError = {
                error = it.message
                isLoading = false
            }
        )
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(loaderSize),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        error != null -> {
            Text(
                text = "Error: $error",
                color = Color.Red,
                modifier = Modifier.padding(errorPadding)
            )
        }

        games.isEmpty() -> {
            Text(
                text = "No games available",
                modifier = Modifier.padding(noCampaignPadding)
            )
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(gameListArrangementSpace)
            ) {
                games.forEachIndexed { index, game ->
                    GameOfferItem(game = game, rank = index + 1)
                }
            }
        }
    }
}

@Composable
fun GameOfferItem(game: BannerData, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = gameListVerticalPadding),
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
                            text = "Top $rank",
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
                        text = game.points,
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
                text = "Play Now",
                fontSize = gameListButtonFontSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MyGamesButton() {
    Button(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = myGamesButtonPadding8
            )
            .height(myGamesButtonHeight),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        shape = RoundedCornerShape(myGamesButtonCornerRadius)
    ) {
        Text(
            text = "My Games",
            fontSize = myGamesButtonFontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameOffersScreenPreview() {
    GameOffersScreen()
}