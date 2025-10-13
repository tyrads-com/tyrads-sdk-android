package com.tyrads.sdk.acmo.modules.legal

import AcmoConfig
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
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
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.helpers.acmoLaunchURL
import com.tyrads.sdk.acmo.core.services.LocalizationService

@Composable
fun AcmoPrivacyPolicyPage(
    onAccepted: (() -> Unit)? = null,
    onCancelled: (() -> Unit)? = null,
    returnToWidget: Boolean? = false
) {
    val scrollState = rememberScrollState()
    val activityContext = LocalContext.current as? ComponentActivity

    // Initialize LocalizationService similar to Flutter implementation
    val localizationService = LocalizationService.getInstance()

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
            Body(localizationService)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height((LocalConfiguration.current.screenHeightDp - 600).dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 36.dp)
            ) {
                Info(localizationService)
            }

            TwoButtonsWithInfo2(
                localizationService = localizationService,
                acceptOnTap = {
                    if (returnToWidget == true) {
                        onAccepted?.invoke()
                    } else {
                        Tyrads.getInstance().setPrivacyAccepted(true)
                        Tyrads.getInstance().navController.navigate("usage-permissions")
                    }
                },
                rejectOntap = {
                    if (returnToWidget == true) {
                        onCancelled?.invoke()
                    } else {
                        activityContext?.finish()
                    }
                },
            )
        }
    }
}

@Composable
fun CloseonTap() {
    val activityContext = LocalContext.current as? ComponentActivity
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
fun Body(localizationService: LocalizationService) {
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

    val windowSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current

    val windowWidthInDp = with(density) {
        windowSize.width.toDp()
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title using localization
        Text(
            modifier = Modifier.width(
                (windowWidthInDp.value * 0.5).dp
            ),
            text = localizationService.translate("data.initialization.intro.title"),
            style = TextStyle(
                fontFamily = lexendFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                color = Color.Black,
            )
        )

        Spacer(modifier = Modifier.height(25.dp))

        Image(
            painter = painterResource(id = R.drawable.privacy_banner),
            contentDescription = "Privacy Banner",
            modifier = Modifier
                .height(160.dp)
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Subtitle using localization
        Text(
            modifier = Modifier.width(
                (windowWidthInDp.value * 0.6).dp
            ),
            text = localizationService.translate("data.initialization.intro.subtitle"),
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
fun Info(localizationService: LocalizationService) {
    val context = LocalContext.current

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

    // Get the localized text which should contain the URL links
    val localizedText = localizationService.translate("data.initialization.legal.explanation")

    // Create clickable text with the localized content
    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontFamily = openSansFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0x9B000000)
            )
        ) {
            append(localizedText)
        }

        val urlPattern = "https://[\\w.-]+(?:\\.[a-zA-Z]{2,})+(?:/[\\w.-]*)*/?".toRegex()
        urlPattern.findAll(localizedText).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            addStringAnnotation(
                tag = "URL",
                annotation = matchResult.value,
                start = start,
                end = end
            )

            addStyle(
                style = SpanStyle(color = Tyrads.getInstance().mainColor?.toColor() ?: Color( AcmoConfig.SECONDARY_COLOR)),
                start = start,
                end = end
            )
        }
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
fun Info2(localizationService: LocalizationService) {
    val context = LocalContext.current
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

    val localizedText = localizationService.translate("data.initialization.intro.label.iHaveRead")

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontFamily = interFontFamily)) {
            val tosPattern = "<tos>(.*?)</tos>".toRegex()
            val ppPattern = "<pp>(.*?)</pp>".toRegex()

            val allMatches = (tosPattern.findAll(localizedText) + ppPattern.findAll(localizedText))
                .sortedBy { it.range.first }

            var currentIndex = 0

            allMatches.forEach { matchResult ->
                append(localizedText.substring(currentIndex, matchResult.range.first))

                val tag = if (matchResult.value.startsWith("<tos>")) "TOS" else "PP"
                val annotation = if (tag == "TOS") {
                    "https://tyrads.com/tyrsdk-terms-of-service/"
                } else {
                    "https://tyrads.com/tyrsdk-privacy-policy/"
                }
                val linkText = matchResult.groupValues[1]

                pushStringAnnotation(tag = tag, annotation = annotation)
                withStyle(style = SpanStyle(color = Tyrads.getInstance().mainColor?.toColor() ?: Color( AcmoConfig.SECONDARY_COLOR))) {
                    append(linkText)
                }
                pop()

                currentIndex = matchResult.range.last + 1
            }

            if (currentIndex < localizedText.length) {
                append(localizedText.substring(currentIndex))
            }
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
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
    localizationService: LocalizationService,
    acceptOnTap: () -> Unit,
    rejectOntap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(
                bottom = 50.dp,
                top = 20.dp,
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 30.dp)
        ) {
            Info2(localizationService)
        }

        Button(
            onClick = acceptOnTap,
            modifier = Modifier
                .width(160.dp)
                .height(35.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Tyrads.getInstance().mainColor?.toColor() ?: Color( AcmoConfig.SECONDARY_COLOR))
        ) {
            // Accept button using localization
            Text(localizationService.translate("data.initialization.intro.cta.accept"))
        }

        TextButton(onClick = rejectOntap) {
            // Reject button using localization
            Text(
                localizationService.translate("data.initialization.intro.cta.reject"),
                color = Color(0xFFB32C2C),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}