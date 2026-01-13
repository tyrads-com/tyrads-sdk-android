package com.tyrads.sdk.acmo.modules.inApp_Notifications//package com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
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
//import com.tyrads.sdk.R
//import com.tyrads.sdk.acmo.modules.notifications.inApp_Notifications.widgets.CommonBonusCard
//
//// Color definitions
//private val CyanButton = Color(0xFF00BCD4)
//
//data class ActivationGame(
//    val id: String,
//    val name: String,
//    val iconRes: Int
//)
//
//@Composable
//fun ActivationBonusScreen(
//    currentCount: Int = 0,
//    maxCount: Int = 3,
//    onDismiss: () -> Unit = {},
//    games: List<ActivationGame> = sampleActivationGames(),
//    coinIconRes: Int = R.drawable.activation_coin
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
//                title = "Activation Bonus",
//                description = {
//                    Text(
//                        text = buildAnnotatedString {
//                            append("Activate your first game and get ")
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
//                maxHeight = 364,
//                bottomLimitLabel = {
//                    Text(
//                        text = buildAnnotatedString {
//                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
//                                append("Daily limit: ")
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
//                    ActivationGameItem(game = game)
//                    Spacer(modifier = Modifier.height(10.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ActivationGameItem(game: ActivationGame) {
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
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    brush = cardGradient,
//                    shape = RoundedCornerShape(12.dp)
//                )
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 12.dp, vertical = 8.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(end = 8.dp) // Prevent overlap with button
//                ) {
//                    // Game icon
//                    Box(
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(RoundedCornerShape(10.dp))
//                            .background(
//                                Brush.linearGradient(
//                                    colors = listOf(
//                                        Color(0xFFFF9800),
//                                        Color(0xFFFF6F00)
//                                    )
//                                )
//                            )
//                    )
//
//                    Spacer(modifier = Modifier.width(10.dp))
//
//                    Text(
//                        text = game.name,
//                        fontSize = 13.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color(0xFF1A1A1A),
//                        maxLines = 2, // Allow wrapping to 2 lines
//                        lineHeight = 15.sp // Minimal line height for tight wrapping
//                    )
//                }
//
//                // Play Now button
//                Button(
//                    onClick = { /* Handle activation */ },
//                    modifier = Modifier
//                        .height(32.dp)
//                        .widthIn(min = 85.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = CyanButton
//                    ),
//                    shape = RoundedCornerShape(5.dp),
//                    elevation = ButtonDefaults.buttonElevation(
//                        defaultElevation = 2.dp,
//                        pressedElevation = 4.dp
//                    ),
//                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
//                ) {
//                    Text(
//                        text = "Play Now",
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White
//                    )
//                }
//            }
//        }
//    }
//}
//
//// Sample data
//fun sampleActivationGames(): List<ActivationGame> {
//    return listOf(
//        ActivationGame(
//            id = "1",
//            name = "Lords Mobile: Kingdom War",
//            iconRes = 0
//        ),
//        ActivationGame(
//            id = "2",
//            name = "Raid: Shadow Legends",
//            iconRes = 0
//        ),
//        ActivationGame(
//            id = "3",
//            name = "Dice Dreams",
//            iconRes = 0
//        )
//    )
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun ActivationBonusScreenPreview() {
//    MaterialTheme {
//        ActivationBonusScreen(
//            currentCount = 0,
//            maxCount = 3,
//            onDismiss = {},
//            games = sampleActivationGames()
//        )
//    }
//}