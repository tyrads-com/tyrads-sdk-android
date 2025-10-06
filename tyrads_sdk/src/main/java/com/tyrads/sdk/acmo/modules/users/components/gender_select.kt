package com.tyrads.sdk.acmo.modules.users.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.core.services.LocalizationService

data class Gender(
    val name: String,
    val isSelected: Boolean,
    val isMale: Boolean = false,
    val isFemale: Boolean = false
)

@Composable
fun AcmoComponentGenderSelector(
    selectedGender: Int?,
    onGenderSelected: (Int) -> Unit
) {
    // Initialize LocalizationService similar to Flutter implementation
    val localizationService = LocalizationService.getInstance()

    // Create genders list with localized names
    val genders = listOf(
        Gender(
            name = localizationService.translate("data.initialization.userInfo.gender.male"),
            isSelected = selectedGender == 1,
            isMale = true
        ),
        Gender(
            name = localizationService.translate("data.initialization.userInfo.gender.female"),
            isSelected = selectedGender == 2,
            isFemale = true
        )
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(genders.size) { index ->
            val gender = genders[index]
            val genderValue = if (index == 0) 1 else 2 // Male = 1, Female = 2

            GenderListItem(
                gender = gender,
                onClick = { onGenderSelected(genderValue) }
            )
        }
    }
}

@Composable
private fun GenderListItem(
    gender: Gender,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (gender.isSelected)
                Tyrads.getInstance().mainColor?.toColor() ?: Color( AcmoConfig.SECONDARY_COLOR)
            else
                Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .size(102.dp)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (gender.isMale) R.drawable.male else R.drawable.female
                    ),
                    contentDescription = gender.name,
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(
                        if (gender.isSelected) Color.White else Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = gender.name,
                    color = if (gender.isSelected) Color.White else Color(0xFF667085),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}