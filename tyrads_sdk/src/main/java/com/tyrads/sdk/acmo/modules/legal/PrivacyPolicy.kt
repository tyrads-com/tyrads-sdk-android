package com.tyrads.sdk.acmo.modules.legal

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.helpers.acmoLaunchURL

@Composable
fun AcmoPrivacyPolicyPage() {
    val scrollState = rememberScrollState()
    val activityContext = LocalContext.current as? ComponentActivity // Get the current context

    Scaffold(    containerColor = Color.White
) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                CloseonTap()
                Body()
                Info()
                Info2()
                Spacer(modifier = Modifier.height(300.dp))
            }
            TwoButtons(
                acceptOnTap = {
                    Tyrads.getInstance().navController.navigate("usage-permissions")

                },
                rejectOntap = {
                   activityContext?.finish()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 38.dp)
            )

        }
    }
}

@Composable
fun CloseonTap() {
    val activityContext = LocalContext.current as? ComponentActivity // Get the current context
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp, top = 10.dp)
            ) {
                IconButton(
                    onClick = {
                        activityContext?.finish()
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFFC4C4C4)
            )
        }
    }
}

@Composable
fun Body() {
    
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val lexendFontName = GoogleFont("Lexend")

val lexendFontFamily = FontFamily(
    Font(googleFont = lexendFontName, fontProvider = provider),
    Font(googleFont = lexendFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = lexendFontName, fontProvider = provider, weight = FontWeight.Bold)
)
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You're So Close To Earning\nYour First Reward!",
            style = TextStyle(
                fontFamily = lexendFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(25.dp))
        Image(
            painter = painterResource(id = R.drawable.privacy_banner),
            contentDescription = "Privacy Banner",
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Play Your Favorite Games!\nAnd Earn Your Rewards!",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = lexendFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Center

            ),
            textAlign = TextAlign.Center,
            lineHeight = 22.4.sp
        )
    }
}

@Composable
fun Info() {
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
    val openSansFontName = GoogleFont("Open Sans")

    val openSansFontFamily = FontFamily(
        Font(googleFont = openSansFontName, fontProvider = provider),
        Font(googleFont = openSansFontName, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = openSansFontName, fontProvider = provider, weight = FontWeight.Bold)
    )
    Text(
        text = "You agree that TyrSDK processes the following personal data within the framework of the use of TyrSDK:\n\n" +
                "Information regarding installed applications, including usage duration and history.\n\n" +
                "This data will be securely encrypted before transmission to our servers and associated with your Device ID",
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.Black.copy(alpha = 0.61f),
            fontFamily = openSansFontFamily,
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 25.dp)
    )
}

@Composable
fun Info2() {
    val context = LocalContext.current  // Get the current context
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
    val interFontName = GoogleFont("Inter")

    val interFontFamily = FontFamily(
        Font(googleFont = interFontName, fontProvider = provider),
        Font(googleFont = interFontName, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = interFontName, fontProvider = provider, weight = FontWeight.Bold)
    )
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontFamily = interFontFamily)) {
            append("I have read and agree to the\n")
            pushStringAnnotation(tag = "TOS", annotation = "https://tyrads.com/tyrsdk-terms-of-service/")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append("Terms of Service")
            }
            pop()
            append(" and ")
            pushStringAnnotation(tag = "PP", annotation = "https://tyrads.com/tyrsdk-privacy-policy/")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append("Privacy Policy")
            }
            pop()
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp)
    ) { offset ->
        annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
            when (annotation.tag) {
                "TOS" -> acmoLaunchURL(context,  annotation.item)
               "PP" -> acmoLaunchURL(context,annotation.item)
            }
        }
    }
}

@Composable
fun TwoButtons(
    acceptOnTap: () -> Unit,
    rejectOntap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = acceptOnTap,
            modifier = Modifier
                .width(160.dp)
                .height(35.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Accept")
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = rejectOntap) {
            Text(
                "Reject",
                color = Color(0xFFB32C2C),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}
