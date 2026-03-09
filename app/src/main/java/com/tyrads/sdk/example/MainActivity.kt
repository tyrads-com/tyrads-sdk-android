package com.tyrads.sdk.example

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.TyradsConfig
import com.tyrads.sdk.example.ui.theme.TyradsSdkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

private const val DEFAULT_USER_ID = "acmo_user_01"
private const val DEFAULT_CONFIG = "belanda1"
private data class ConfigKeys(val apiKey: String, val apiSecret: String, val encKey: String)

private val configOptions = listOf(
    "tyrreward" to "Tyrreward",
    "belanda1" to "Belanda 1",
    "belanda2" to "Belanda 2",
    "belanda3" to "Belanda 3",
)

private fun getConfigKeys(selectedConfig: String): ConfigKeys = when (selectedConfig) {
    "tyrreward" -> ConfigKeys(
        apiKey = BuildConfig.TYRREWARD_API_KEY,
        apiSecret = BuildConfig.TYRREWARD_API_SECRET,
        encKey = BuildConfig.TYRREWARD_ENC_KEY,
    )
    "belanda2" -> ConfigKeys(
        apiKey = BuildConfig.BELANDA2_API_KEY,
        apiSecret = BuildConfig.BELANDA2_API_SECRET,
        encKey = BuildConfig.BELANDA2_ENC_KEY,
    )
    "belanda3" -> ConfigKeys(
        apiKey = BuildConfig.BELANDA3_API_KEY,
        apiSecret = BuildConfig.BELANDA3_API_SECRET,
        encKey = BuildConfig.BELANDA3_ENC_KEY,
    )
    else -> ConfigKeys(
        apiKey = BuildConfig.BELANDA1_API_KEY,
        apiSecret = BuildConfig.BELANDA1_API_SECRET,
        encKey = BuildConfig.BELANDA1_ENC_KEY,
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TyradsSdkTheme {
                var reloadKey by remember { mutableIntStateOf(0) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    key(reloadKey) {
                        Greeting(
                            modifier = Modifier.padding(innerPadding),
                            onReload = {
                                reloadKey++
                                Log.i("Reload", reloadKey.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier, onReload: () -> Unit = {}) {
    val context = LocalContext.current
    var isLoadingOffers by remember { mutableStateOf(false) }
    var fcmToken: String? by remember { mutableStateOf("") }

    val sharedPreferences = context.getSharedPreferences("TyradsPrefs", Context.MODE_PRIVATE)
    val sdkPrefs = context.getSharedPreferences("tyrads_sdk_prefs", Context.MODE_PRIVATE)

    var selectedConfig by remember {
        mutableStateOf(sharedPreferences.getString("selectedConfig", DEFAULT_CONFIG) ?: DEFAULT_CONFIG)
    }

    val initialKeys = remember(selectedConfig) { getConfigKeys(selectedConfig) }

    var apiKeyInput by remember { mutableStateOf(initialKeys.apiKey) }
    var apiSecretInput by remember { mutableStateOf(initialKeys.apiSecret) }
    var encryptionKey by remember { mutableStateOf(initialKeys.encKey) }
    var engagementId by remember {
        mutableStateOf(sharedPreferences.getString("engagementId", "") ?: "")
    }
    var placementId by remember {
        mutableStateOf(sharedPreferences.getString("placementId", "") ?: "")
    }
    var userIdInput by remember {
        mutableStateOf(sharedPreferences.getString("userId", DEFAULT_USER_ID) ?: DEFAULT_USER_ID)
    }

    var loggedIn by remember { mutableStateOf(false) }
    var lastInitializedUserId by remember { mutableStateOf(userIdInput) }
    var widgetReloadKey by remember { mutableIntStateOf(0) }

    val clipboard = LocalClipboard.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val options = listOf("Show", "Hide")
    var selectedOption by remember { mutableStateOf(options[0]) }
    var lastSelectedOption by remember { mutableStateOf(selectedOption) }

    LaunchedEffect(selectedOption) {
        fcmToken = sdkPrefs.getString("acmo_tyrads_sdk_fcm_token", null)
        Tyrads.getInstance().init(
            context,
            apiKey = apiKeyInput.ifBlank { initialKeys.apiKey },
            apiSecret = apiSecretInput.ifBlank { initialKeys.apiSecret },
            encryptionKey = encryptionKey.ifBlank { initialKeys.encKey },
            engagementId = engagementId,
            placementId = placementId,
            config = TyradsConfig(skipInitialPages = selectedOption == options[1]),
        )
        val success = Tyrads.getInstance().loginUser(userID = userIdInput.ifBlank { DEFAULT_USER_ID })
        loggedIn = success
        lastInitializedUserId = userIdInput
    }

    fun onConfigChange(newConfig: String) {
        val newKeys = getConfigKeys(newConfig)
        selectedConfig = newConfig
        apiKeyInput = newKeys.apiKey
        apiSecretInput = newKeys.apiSecret
        encryptionKey = newKeys.encKey

        sharedPreferences.edit { putString("selectedConfig", newConfig) }

        // Using composable-scoped coroutine (scope.launch) instead of CoroutineScope(Dispatchers.Main).launch
        // to avoid race conditions on rapid config switches — revert to CoroutineScope if needed.
        scope.launch {
            Tyrads.getInstance().init(
                context,
                apiKey = newKeys.apiKey,
                apiSecret = newKeys.apiSecret,
                encryptionKey = newKeys.encKey,
                engagementId = engagementId,
                placementId = placementId,
                config = TyradsConfig(skipInitialPages = selectedOption == options[1]),
            )
            val success = Tyrads.getInstance().loginUser(userID = userIdInput.ifBlank { DEFAULT_USER_ID })
            if (success) {
                lastInitializedUserId = userIdInput
                widgetReloadKey++
            }
        }
    }

    fun handleButtonClick() {
        if (userIdInput == lastInitializedUserId && selectedOption == lastSelectedOption) {
            scope.launch {
                Tyrads.getInstance().showOffers()
            }
            return
        }

        isLoadingOffers = true
        CoroutineScope(Dispatchers.Main).launch {
            sharedPreferences.edit().apply {
                putString("apiKey", apiKeyInput)
                putString("apiSecret", apiSecretInput)
                putString("userId", userIdInput)
                putString("encryptionKey", encryptionKey)
                putString("placementId", placementId)
                apply()
            }

            Tyrads.getInstance().init(
                context,
                apiKey = apiKeyInput.ifBlank { initialKeys.apiKey },
                apiSecret = apiSecretInput.ifBlank { initialKeys.apiSecret },
                encryptionKey = encryptionKey.ifBlank { initialKeys.encKey },
                engagementId = engagementId,
                placementId = placementId,
                config = TyradsConfig(skipInitialPages = selectedOption == options[1]),
                debugMode = false,
            )

            val isSuccess = Tyrads.getInstance().loginUser(userID = userIdInput.ifBlank { DEFAULT_USER_ID })
            isLoadingOffers = false
            if (!isSuccess) return@launch
            Tyrads.getInstance().showOffers()
            lastInitializedUserId = userIdInput
            lastSelectedOption = selectedOption
            widgetReloadKey++
        }
    }

    if (!loggedIn) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row {
                Text(text = "Tyrads SDK Example", modifier = modifier)
            }

            key(widgetReloadKey) {
                Tyrads.getInstance().TopPremiumOffers(
                    widgetStyle = Tyrads.PremiumWidgetStyles.SLIDER_CARDS
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SimpleDropdown(
                options = options,
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConfigDropdown(
                selectedConfig = selectedConfig,
                onConfigSelected = { onConfigChange(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Enter API Key") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = apiSecretInput,
                onValueChange = { apiSecretInput = it },
                label = { Text("Enter API Secret") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = encryptionKey,
                onValueChange = { encryptionKey = it },
                label = { Text("Encryption Key (optional)") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = engagementId,
                onValueChange = { engagementId = it },
                label = { Text("Engagement ID (optional)") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = placementId,
                onValueChange = { placementId = it },
                label = { Text("Placement ID (optional)") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = userIdInput,
                onValueChange = { userIdInput = it },
                label = { Text("Enter User ID") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (fcmToken != null)
                TextField(
                    value = fcmToken ?: "NA",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("FCM Token") },
                    singleLine = false,
                )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { handleButtonClick() },
                    modifier = Modifier.padding(16.dp),
                ) {
                    if (isLoadingOffers) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = "Show Offers")
                }

                if (fcmToken != null)
                    Button(
                        onClick = {
                            scope.launch {
                                val clipEntry = ClipEntry(ClipData.newPlainText("label", fcmToken))
                                clipboard.setClipEntry(clipEntry)
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar("Token copied!")
                                snackbarHostState.showSnackbar(
                                    "Token copied!",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                    ) {
                        Text("Copy Token", fontWeight = FontWeight.SemiBold)
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigDropdown(
    selectedConfig: String,
    onConfigSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = configOptions.firstOrNull { it.first == selectedConfig }?.second ?: selectedConfig

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select Config:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333),
        )
        Spacer(modifier = Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                readOnly = true,
                value = selectedLabel,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                configOptions.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onConfigSelected(value)
                            expanded = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Platform: Android | Config: ${selectedConfig.uppercase()}",
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF666666),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text("Select Initial Pages Settings") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TyradsSdkTheme {
        Greeting()
    }
}