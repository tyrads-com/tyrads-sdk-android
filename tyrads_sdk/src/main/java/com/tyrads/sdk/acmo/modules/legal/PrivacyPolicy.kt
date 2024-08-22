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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.text.style.TextDecoration
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

    val context = LocalContext.current
    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CloseonTap()
                Body()
                Column(
                    modifier = Modifier
                        .height((LocalConfiguration.current.screenHeightDp - 600).dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 30.dp)
                ) {
                    Info()
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Info2(
                    )
                }
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
            .padding(end = 20.dp)
    ) {
        IconButton(
            onClick = {
                activityContext?.finish()
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
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
                textAlign = TextAlign.Center,
                color = Color.Black

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
                textAlign = TextAlign.Center,
                color = Color.Black


            ),
            textAlign = TextAlign.Center,
            lineHeight = 22.4.sp
        )
    }
}

@Composable
fun Info() {
    val context = LocalContext.current  // Get the current context

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
    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontFamily = openSansFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0x9B000000)
            )
        ) {
            append(
                "We hereby inform you that Tyrads Pte. Ltd. processes the following personal data within the framework of the use of TyrSDK:\n" +
                        "Installed apps (including the use duration and use history)\n" +
                        "The data is linked to your device via the device ID (GAID or IDFA) transmitted to our servers in encrypted form. So that app providers can finance our app suggestions, we must send them the device ID for billing purposes.\n\n" +
                        "The processing of the above data is necessary to be able to recommend apps via system messages, the installation of apps available in TyrSDK that matches your interest and calculate the rewards acquired as a result of your use of the corresponding apps.\n\n" +
                        "Consent\n\n" +
                        "By clicking on 'Accept' I give Tyrads Pte. Ltd my consent to process above mentioned personal data and transmit it to other apps so that i can use TyrSDK as explained.\n\n" +
                        "I am aware that the above data results in an interest profile, which, depending on the type of apps I use, may contain particularly sensitive personal data (such as health data or data on my sexual orientation as well as any other data from special categories defined in Art. 9 para. 1 of the European General Data Protection Regulation (GDPR).\n\n" +
                        "This data will be processed by Tyrads Pte. Ltd, TyrSDK. For more information "
            )
        }
        withStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("https://tyrads.com/tyrsdk-privacy-policy/")
        }
        addStringAnnotation(
            tag = "URL",
            annotation = "https://tyrads.com/tyrsdk-privacy-policy/",
            start = length - "https://tyrads.com/tyrsdk-privacy-policy/".length,
            end = length
        )
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                acmoLaunchURL(context, it.item)
            }
        }
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
            pushStringAnnotation(
                tag = "TOS",
                annotation = "https://tyrads.com/tyrsdk-terms-of-service/"
            )
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append("Terms of Service")
            }
            pop()
            append(" and ")
            pushStringAnnotation(
                tag = "PP",
                annotation = "https://tyrads.com/tyrsdk-privacy-policy/"
            )
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
                "TOS" -> acmoLaunchURL(context, annotation.item)
                "PP" -> acmoLaunchURL(context, annotation.item)
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
        modifier = modifier.padding(bottom = 38.dp),
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
