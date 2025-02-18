package com.tyrads.sdk.acmo.modules.legal.settings

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tyrads.sdk.acmo.core.localization.helper.LocalizationHelper

@Composable
fun LanguageDropdownMenu(
) {

    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(LocalizationHelper.getLanguageCode(context)) }
    var expanded by remember { mutableStateOf(false) }

    val languages = listOf(
        Language("English", "en"),
        Language("Spanish", "es"),
        Language("Korean", "ko"),
        Language("Japanese", "ja"),
        Language("Indonesian", "id"),
        Language("Device Default", "default")
    )



    Box(modifier = Modifier.wrapContentSize()) {
        Button(onClick = { expanded = true }) {
            Text(text = languages.find { it.code == selectedLanguage }?.name ?: "Select Language")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {
//                        if(language.code == "default") {
//                            LocalizationHelper.setDeviceDefaultLanguage(context)
//                        }
//                        else {
//                            LocalizationHelper.changeLanguage(context, language.code)
//                        }
                        selectedLanguage = language.code
                        expanded = false
                    }
                )
            }
        }
    }
    LaunchedEffect(selectedLanguage) {
        Log.e("Localization", "Language changed to: $selectedLanguage")
        if (selectedLanguage == "default") {
            LocalizationHelper.setDeviceDefaultLanguage(context)
        } else {
            LocalizationHelper.changeLanguage(context, selectedLanguage)
        }
    }
}

data class Language(
    val name: String,
    val code: String
)
