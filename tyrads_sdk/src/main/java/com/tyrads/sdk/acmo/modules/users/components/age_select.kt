package com.tyrads.sdk.acmo.modules.users.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AcmoComponentAgeSelector(
    onChanged: (Int) -> Unit,
    init: Int = 18,
    min: Int = 13,
    modifier: Modifier = Modifier
) {
    AcmoHorizontalNumberPicker(
        modifier = modifier,
        minValue = min,
        initialValue = init,
        onValueChange = onChanged
    )
}
