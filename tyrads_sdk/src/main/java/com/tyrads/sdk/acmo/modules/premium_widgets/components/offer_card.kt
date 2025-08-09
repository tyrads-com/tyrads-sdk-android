package com.tyrads.sdk.acmo.modules.premium_widgets.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tyrads.sdk.R
import com.tyrads.sdk.acmo.modules.dashboard.components.formatNumeral
import com.tyrads.sdk.acmo.modules.input_models.AcmoOfferCurrencySaleModel
import com.tyrads.sdk.acmo.modules.input_models.AcmoOffersModel

// CardContainer Composable
@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    padding: PaddingValues? = null,
    borderRadius: Float? = null,
    height: Float? = null,
    width: Float? = null,
    decoration: androidx.compose.ui.graphics.Shape? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .then(
                if (height != null) Modifier.height(with(density) { height.dp })
                else Modifier
            )
            .then(
                if (width != null) Modifier.width(with(density) { width.dp })
                else Modifier
            )
            .shadow(
                elevation = 16.dp,
                shape = decoration ?: RoundedCornerShape((borderRadius ?: 10f).dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .background(
                color = Color.White,
                shape = decoration ?: RoundedCornerShape((borderRadius ?: 10f).dp)
            )
            .then(
                if (padding != null) Modifier.padding(padding)
                else Modifier
            ),
        content = content
    )
}

// Triangle Painter equivalent using Canvas
@Composable
fun TrianglePainter(
    color: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.geometry.Size = androidx.compose.ui.geometry.Size(20f, 40f)
) {
    Canvas(
        modifier = modifier.size(
            width = with(LocalDensity.current) { size.width.dp },
            height = with(LocalDensity.current) { size.height.dp }
        )
    ) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            close()
        }

        drawPath(
            path = path,
            color = color
        )
    }
}

// Main AcmoOfferCard Composable
@Composable
fun AcmoOfferCard(
    item: AcmoOffersModel,
    onButtonClick: (() -> Unit)?,
    currencySaleModel: AcmoOfferCurrencySaleModel,
    itemScaleFactor: Double = 1.93,
    margin: PaddingValues? = null,
    isPremiumWidget: Boolean = false,
    onTap: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val itemHeight = screenWidth.value / itemScaleFactor.toFloat()

    Box(
        modifier = Modifier
            .clickable { onTap?.invoke() }
    ) {
        // Triangle painter positioned at top left
        if (currencySaleModel.data?.currencySales != null) {
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 63.dp)
            ) {
                TrianglePainter(
                    color = getDarkerShade(
                        Tyrads.instance.colorPremium
                            ?: MaterialTheme.colorScheme.secondary
                    ),
                    size = androidx.compose.ui.geometry.Size(20f, 40f)
                )
            }
        }

        // Main card container
        CardContainer(
            modifier = Modifier.padding(
                margin ?: PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 0.dp
                )
            ),
            borderRadius = 16f,
            height = itemHeight + 112f
        ) {
            // Main image with rounded top corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    )
            ) {
                AsyncImage(
                    model = item.creative.creativePacks.firstOrNull()
                        ?.creatives?.firstOrNull()?.fileUrl ?: "",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Premium diamond icon
                if (item.premium) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-16).dp, y = 16.dp)
                            .size(28.dp)
                            .background(
                                color = Color(0xD91E2020),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.diamond), // Replace with actual resource
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                        )
                    }
                }
            }

            // Bottom card section
            Box(
                modifier = Modifier
                    .offset(y = (itemHeight - 16).dp)
                    .fillMaxWidth()
            ) {
                CardContainer(
                    borderRadius = 16f,
                    height = 128f
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 5.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // ListTile equivalent
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Leading image
                            AsyncImage(
                                model = item.app.thumbnail,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Title
                            Text(
                                text = item.app.title,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = (18f/14f * 14).sp,
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            // Trailing content
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    // Strike-through price if currency sales exist
                                    if (currencySaleModel.data?.currencySales != null) {
                                        Text(
                                            text = item.campaignPayout.totalPlayablePayoutConverted
                                                .formatNumeral(digits = 2),
                                            style = TextStyle(
                                                color = Color(0xFF454646),
                                                fontWeight = FontWeight.W300,
                                                fontSize = 12.sp,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        )
                                    }

                                    // Current price with currency icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = item.currency.adUnitCurrencyIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Text(
                                            text = " ${(item.campaignPayout.totalPlayablePayoutConverted *
                                                    (currencySaleModel.data?.currencySales?.multiplier ?: 1.0))
                                                .formatNumeral(digits = 2)}",
                                            style = TextStyle(
                                                color = Color.Black,
                                                fontWeight = FontWeight.W700,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(15.dp))

                                // Info icon
                                Image(
                                    painter = painterResource(R.drawable.info), // Replace with actual resource
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            onTap?.invoke() ?: run {
                                                // Default navigation action
                                            }
                                        }
                                )
                            }
                        }

                        // Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.5.dp)
                        ) {
                            AcmoButton3(
                                onTap = onButtonClick,
                                label = "Offers CTA", // Replace with actual translation
                                borderRadius = 8.0,
                                labelStyle = TextStyle(
                                    color = Tyrads.instance.colorPremiumFg ?: Color.White,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 14.sp
                                ),
                                color = if (currencySaleModel.data?.currencySales != null || isPremiumWidget) {
                                    Tyrads.instance.colorPremium
                                        ?: MaterialTheme.colorScheme.secondary
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bonus label if currency sales exist
        if (currencySaleModel.data?.currencySales != null) {
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 32.dp)
                    .height(31.dp)
                    .background(
                        color = Tyrads.instance.colorPremium
                            ?: MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(
                            topStart = 50.dp,
                            topEnd = 100.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 100.dp
                        )
                    )
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(
                            topStart = 50.dp,
                            topEnd = 100.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 100.dp
                        ),
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${currencySaleModel.data?.currencySales?.multiplier?.formatDouble()}x Bonus",
                    style = TextStyle(
                        fontWeight = FontWeight.W700,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        color = Tyrads.instance.colorPremiumFg ?: Color.White
                    )
                )
            }
        }
    }
}



fun Double.formatDouble(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
    }
}

// Helper function for getting darker shade
fun getDarkerShade(color: Color): Color {
    return color.copy(
        red = (color.red * 0.8f).coerceAtLeast(0f),
        green = (color.green * 0.8f).coerceAtLeast(0f),
        blue = (color.blue * 0.8f).coerceAtLeast(0f)
    )
}

// AcmoButton3 component - exact conversion of AcmoButton_3
@Composable
fun AcmoButton3(
    onTap: (() -> Unit)?,
    label: String = "Edit",
    color: Color? = null,
    borderRadius: Double? = null,
    labelStyle: TextStyle? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onTap?.invoke() }
            .background(
                color = color ?: MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape((borderRadius?.toFloat() ?: 4f).dp)
            )
            .size(width = 152.dp, height = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = labelStyle ?: TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Placeholder for Tyrads instance (implement based on your actual singleton)
object Tyrads {
    val instance = TyradsInstance()
}

class TyradsInstance {
    val colorPremium: Color? = Color(0xFF6C5CE7) // Purple color for preview
    val colorPremiumFg: Color? = Color.White
}
