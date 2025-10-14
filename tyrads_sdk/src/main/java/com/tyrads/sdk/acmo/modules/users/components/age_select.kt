package com.tyrads.sdk.acmo.modules.users.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AcmoComponentAgeSelector(
    modifier: Modifier = Modifier,
    onChanged: (Int) -> Unit,
    init: Int = 18,
    min: Int = 13
) {
    AcmoHorizontalNumberPicker(
        modifier = modifier,
        minValue = min,
        initialValue = init,
        onValueChange = onChanged
    )
}