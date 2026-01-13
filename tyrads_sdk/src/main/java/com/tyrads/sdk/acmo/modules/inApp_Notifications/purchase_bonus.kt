package com.tyrads.sdk.acmo.modules.inApp_Notifications//package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.withStyle
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import androidx.compose.ui.zIndex
//import com.tyrads.sdk.R
//import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets.CommonBonusCard
//
//// Color definitions
//private val CyanButton = Color(0xFF00BCD4)
//
//data class GameOffer(
//    val id: String,
//    val name: String,
//    val iconRes: Int,
//    val offers: List<PurchaseOffer>
//)
//
//data class PurchaseOffer(
//    val points: String,
//    val description: String
//)
//
//@Composable
//fun PurchaseBonusScreen(
//    currentCount: Int = 0,
//    maxCount: Int = 5,
//    onDismiss: () -> Unit = {},
//    games: List<GameOffer> = sampleGames(),
//    coinIconRes: Int = R.drawable.purchase_coin
//) {
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(
//            dismissOnBackPress = true,
//            dismissOnClickOutside = true,
//            usePlatformDefaultWidth = false
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.5f)),
//            contentAlignment = Alignment.Center
//        ) {
//            CommonBonusCard(
//                title = "Purchase Bonus",
//                description = {
//                    Text(
//                        text = buildAnnotatedString {
//                            append("Make a purchase in any activated game and get ")
//                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                                append("1M points")
//                            }
//                            append(" instantly!")
//                        },
//                        fontSize = 13.sp,
//                        color = Color(0xFF1A1A1A),
//                        textAlign = TextAlign.Center,
//                        lineHeight = 18.sp,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                },
//                onDismiss = onDismiss,
//                coinIconRes = coinIconRes,
//                minHeight = 300,
//                maxHeight = 426,
//                bottomLimitLabel = {
//                    Text(
//                        text = buildAnnotatedString {
//                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
//                                append("Purchase limit: ")
//                            }
//                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                                append("$currentCount/$maxCount")
//                            }
//                        },
//                        fontSize = 14.sp,
//                        color = Color(0xFF1A4D4C)
//                    )
//                }
//            ) {
//                // Game list content
//                games.forEach { game ->
//                    GameOfferItem(game = game)
//                    Spacer(modifier = Modifier.height(10.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun GameOfferItem(game: GameOffer) {
//    var expanded by remember { mutableStateOf(false) }
//
//    // Solid gradient background for game card
//    val cardGradient = Brush.verticalGradient(
//        colors = listOf(
//            Color.White,
//            Color(0xFFE8F7F7),
//            Color(0xFFB8E8E8),
//        )
//    )
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .shadow(
//                elevation = 2.dp,
//                shape = RoundedCornerShape(12.dp)
//            ),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.Transparent
//        )
//    ) {
//        Column(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Box(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                // Expandable content - goes behind parent card
//                androidx.compose.animation.AnimatedVisibility(
//                    visible = expanded,
//                    enter = androidx.compose.animation.expandVertically(),
//                    exit = androidx.compose.animation.shrinkVertically()
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 44.dp) // Space for parent card
//                            .background(
//                                Brush.verticalGradient(
//                                    colors = listOf(
//                                        Color(0xFFCEF5F5),
//                                        Color.White,
//                                        Color.White
//                                    )
//                                ),
//                                RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
//                            )
//                            .padding(horizontal = 12.dp, vertical = 6.dp)
//                            .padding(top = 16.dp)
//                    ) {
//                        game.offers.forEachIndexed { index, offer ->
//                            PurchaseOfferRow(offer = offer)
//                            if (index < game.offers.size - 1) {
//                                Spacer(modifier = Modifier.height(2.dp))
//                            }
//                        }
//                    }
//                }
//
//                // Game header - Parent card on top with consistent elevation
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .zIndex(1f)
//                        .shadow(
//                            elevation = 2.dp,
//                            shape = RoundedCornerShape(12.dp)
//                        ),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = Color.Transparent
//                    )
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(
//                                brush = cardGradient,
//                                shape = RoundedCornerShape(12.dp)
//                            )
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable { expanded = !expanded }
//                                .padding(horizontal = 12.dp, vertical = 10.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier.weight(1f)
//                            ) {
//                                // Game icon
//                                Box(
//                                    modifier = Modifier
//                                        .size(36.dp)
//                                        .clip(RoundedCornerShape(10.dp))
//                                        .background(
//                                            Brush.linearGradient(
//                                                colors = listOf(
//                                                    Color(0xFFFF9800),
//                                                    Color(0xFFFF6F00)
//                                                )
//                                            )
//                                        )
//                                )
//
//                                Spacer(modifier = Modifier.width(10.dp))
//
//                                Text(
//                                    text = game.name,
//                                    fontSize = 13.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = Color(0xFF1A1A1A)
//                                )
//                            }
//
//                            // Dropdown arrow
//                            Icon(
//                                painter = painterResource(
//                                    if (expanded) R.drawable.angle_up else R.drawable.angle_down
//                                ),
//                                contentDescription = if (expanded) "Collapse" else "Expand",
//                                modifier = Modifier.size(18.dp),
//                                tint = Color(0xFF1A1A1A)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PurchaseOfferRow(offer: PurchaseOffer) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 10.dp, vertical = 4.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        // Left side: Text and coin icon in column layout
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            // First row: "Make in-app purchase" text
//            Text(
//                text = offer.description,
//                fontSize = 11.sp,
//                fontWeight = FontWeight.Normal,
//                color = Color(0xFF4A4A4A),
//                maxLines = 1,
//                lineHeight = 12.sp
//            )
//
//            // Second row: Coin icon + "200K" points - no gap
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.offset(y = (-1).dp)
//            ) {
//                // Small coin icon image
//                Image(
//                    painter = painterResource(R.drawable.ic_coin2),
//                    contentDescription = "Coin",
//                    modifier = Modifier.size(14.dp),
//                    contentScale = ContentScale.Fit
//                )
//
//                Spacer(modifier = Modifier.width(4.dp))
//
//                Text(
//                    text = offer.points,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF1E2020)
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        // Purchase button with 5dp border radius
//        Button(
//            onClick = { /* Handle purchase */ },
//            modifier = Modifier
//                .height(32.dp)
//                .widthIn(min = 85.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = CyanButton
//            ),
//            shape = RoundedCornerShape(5.dp),
//            elevation = ButtonDefaults.buttonElevation(
//                defaultElevation = 2.dp,
//                pressedElevation = 4.dp
//            ),
//            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
//        ) {
//            Text(
//                text = "Purchase",
//                fontSize = 11.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White
//            )
//        }
//    }
//}
//
//// Sample data
//fun sampleGames(): List<GameOffer> {
//    return listOf(
//        GameOffer(
//            id = "1",
//            name = "Lords Mobile: Kingdom War",
//            iconRes = 0,
//            offers = listOf(
//                PurchaseOffer("200K", "Make in-app purchase"),
//                PurchaseOffer("200K", "Make in-app purchase")
//            )
//        ),
//        GameOffer(
//            id = "2",
//            name = "Dice Dreams",
//            iconRes = 0,
//            offers = listOf()
//        )
//    )
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun PurchaseBonusScreenPreview() {
//    MaterialTheme {
//        PurchaseBonusScreen(
//            currentCount = 0,
//            maxCount = 5,
//            onDismiss = {},
//            games = sampleGames()
//        )
//    }
//}