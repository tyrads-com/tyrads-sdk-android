package com.tyrads.sdk.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.example.ui.theme.*

// Spacing Constants
val spacingXSmall = 3.dp
val spacingSmall = 6.dp
val spacingMedium = 8.dp
val spacingLarge = 16.dp
val spacingXLarge = 25.dp

// Corner Radius Constants
val cornerRadiusSmall = 8.dp
val cornerRadiusMedium = 34.dp
val cornerRadiusLarge = 35.dp

// Icon Sizes
val iconSizeSmall = 17.dp
val iconSizeMedium = 24.dp

// Text Sizes
val textSizeXSmall = 12.sp
val textSizeSmall = 14.sp
val textSizeMedium = 16.sp
val textSizeLarge = 18.sp

// Image Sizes
val imageHeightMedium = 173.dp

// Elevation
val elevationDefault = 8.dp


class PremiumLayoutsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val scrollState = rememberScrollState()
    val toolbarColor = lerp(
        start = DeepTeal,
        stop = DarkerTeal,
        fraction = (scrollState.value / 600f).coerceIn(0f, 1f)
    )

    Scaffold(
        topBar = {
            AppTopBar(backgroundColor = toolbarColor)
        },
        containerColor = Color(0xFF002533)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(0xFF002533))
        ) {
            UserGreetingScreen()
            LayoutsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(backgroundColor: Color) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = { /* Handle menu click */ }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Handle settings click */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor
        ),
        modifier = Modifier.shadow(elevation =  elevationDefault)
    )
}

@Composable
fun UserGreetingScreen() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal =  spacingLarge)
            .padding(top =  spacingXLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Hey,",
                color = Color.White,
                fontSize =  textSizeMedium
            )
            Text(
                text = "Saskia Mauly",
                color = Color.White,
                fontSize =  textSizeLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height( spacingMedium))

            Text(
                text = "Your earnings:",
                color = Color.White,
                fontSize =  textSizeXSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical =  spacingSmall)
                    .background(
                        color = DarkGrayishBlue,
                        shape = RoundedCornerShape(cornerRadiusLarge)
                    )
                    .padding(horizontal =  spacingMedium, vertical =  spacingMedium)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_coin2),
                    contentDescription = "Coin",
                    modifier = Modifier.size( iconSizeSmall)
                )
                Spacer(modifier = Modifier.width( spacingSmall))
                Text(
                    text = "468M TPoints",
                    color = Color.White,
                    fontSize = textSizeSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.plant_coins),
            contentDescription = "Growth Illustration",
            modifier = Modifier
                .weight(0.8f)
                .height( imageHeightMedium)
                .padding(start = spacingXSmall)
        )
    }
}

@Composable
fun LayoutsScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart =  cornerRadiusMedium,
                    topEnd =  cornerRadiusMedium
                )
            )
            .background(Color.White)
    ) {
        Column {
            Tyrads.getInstance().OffersScreen()
            Tyrads.getInstance().OffersScreen4()
            Tyrads.getInstance().GameOffersScreen()
            Tyrads.getInstance().OffersScreen3()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen()
    }
}
