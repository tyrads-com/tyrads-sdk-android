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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.helpers.acmoLaunchURL
import com.tyrads.sdk.acmo.modules.legal.settings.LanguageDropdownMenu

@Composable
fun AcmoPrivacyPolicyPage() {
    val scrollState = rememberScrollState()
    val activityContext = LocalContext.current as? ComponentActivity // Get the current context

    val context = LocalContext.current
    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CloseonTap()
                Body()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height((LocalConfiguration.current.screenHeightDp - 600).dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 30.dp)
                ) {
                    Info()
                }

                TwoButtonsWithInfo2(
                    acceptOnTap = {
                        Tyrads.getInstance().navController.navigate("usage-permissions")

                    },
                    rejectOntap = {
                        activityContext?.finish()
                    },
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
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.privacy_policy_title),
            style = TextStyle(
                fontFamily = lexendFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                color = Color.Black

            )
        )
//        LanguageDropdownMenu()
        Spacer(modifier = Modifier.height(25.dp))
        Image(
            painter = painterResource(id = R.drawable.privacy_banner),
            contentDescription = "Privacy Banner",
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
//            text = "Play Your Favorite Games!\nAnd Earn Your Rewards!",
            text = stringResource(id = R.string.privacy_policy_subtitle),
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
                text = stringResource(id = R.string.privacy_policy_consent_info)
            )
        }
        withStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(" https://tyrads.com/tyrsdk-privacy-policy/")
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
            append(stringResource(id = R.string.privacy_policy_agreement_prefix))
            pushStringAnnotation(
                tag = "TOS",
                annotation = "https://tyrads.com/tyrsdk-terms-of-service/"
            )
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append(stringResource(id = R.string.privacy_policy_terms_text))
            }
            pop()
            append(stringResource(id = R.string.privacy_policy_and))
            pushStringAnnotation(
                tag = "PP",
                annotation = "https://tyrads.com/tyrsdk-privacy-policy/"
            )
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                append(stringResource(id = R.string.privacy_policy_privacy_text))
            }
            pop()
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
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
fun TwoButtonsWithInfo2(
    acceptOnTap: () -> Unit,
    rejectOntap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(bottom = 20.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Info2()
        Button(
            onClick = acceptOnTap,
            modifier = Modifier
                .width(160.dp)
                .height(35.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(stringResource(id = R.string.privacy_policy_accept))
        }
        TextButton(onClick = rejectOntap) {
            Text(
                stringResource(id = R.string.privacy_policy_reject),
                color = Color(0xFFB32C2C),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}
