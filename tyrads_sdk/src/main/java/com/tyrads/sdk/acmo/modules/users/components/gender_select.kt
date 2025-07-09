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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tyrads.sdk.R

@Composable
fun AcmoComponentGenderSelector(
    selectedGender: Int?,
    onGenderSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(2) { index ->
            val genderValue = if (index == 0) 1 else 2 // Male = 1, Female = 2
            val isSelected = selectedGender == genderValue

            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { onGenderSelected(genderValue) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.secondary
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
                        .size(100.dp)
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (index == 0) R.drawable.male else R.drawable.female
                            ),
                            contentDescription = if (index == 0) "Male" else "Female",
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(
                                if (isSelected) Color.White else Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(
                                id = if (index == 0) R.string.male else R.string.female,
                                if (index == 0) "Male" else "Female"
                            ),
                            color = if (isSelected) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}