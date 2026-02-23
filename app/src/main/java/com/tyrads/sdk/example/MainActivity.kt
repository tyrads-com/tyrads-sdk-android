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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.input_models.TyradsConfig
import com.tyrads.sdk.example.ui.theme.TyradsSdkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    var apiKeyInput by remember { mutableStateOf(sharedPreferences.getString("apiKey", "") ?: "") }
    var apiSecretInput by remember {
        mutableStateOf(sharedPreferences.getString("apiSecret", "") ?: "")
    }
    var encryptionKey by remember {
        mutableStateOf(
            sharedPreferences.getString("encryptionKey", "") ?: ""
        )
    }
    var engagementId by remember {
        mutableStateOf(
            sharedPreferences.getString("engagementId", "") ?: ""
        )
    }
    var userIdInput by remember { mutableStateOf(sharedPreferences.getString("userId", "1") ?: "") }

    var loggedIn by remember { mutableStateOf(false) }

    var lastInitializedUserId by remember {
        mutableStateOf(
            sharedPreferences.getString("userId", "") ?: ""
        )
    }
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
            context, apiKey = apiKeyInput.ifBlank { "0a55de10c58f459c9f65988d9d33e774" },
            apiSecret = apiSecretInput.ifBlank { "418fc08c18a6715b48428568946e6f82f0ff06bfbc017944d22a19b3317a5ce2ad7028b0599a149534d957017d54650a9fa355cebf6971d7fdbc3eca372ca4ed" },
            encryptionKey = encryptionKey.ifBlank { "VKdZsSz9&3WQqA6xfBJ4G2!5cUe8Y7yP" },
            engagementId = engagementId,
            config = TyradsConfig(
                skipInitialPages = selectedOption == options[1],
            )
        )
        val success = Tyrads.getInstance().loginUser(userID = userIdInput.ifBlank { "14560" })
        loggedIn = success
        lastInitializedUserId = userIdInput
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
                apply()
            }

            Tyrads.getInstance().init(
                context,
                apiKey = apiKeyInput.ifBlank { "4f0eaa99e38e49b8b52804116e638a41" },
                apiSecret = apiSecretInput.ifBlank { "cd3c34a52a3b75a3fdd928774615d4e142dd2e6a8ce9da14df4205c7cc812ce81d3656e3dc2c0c58ed05c75c57f87a3431fed62725bb0286f9461521b6c9997a" },
                encryptionKey = encryptionKey.ifBlank { "dKWuxV#Ab9pBXNvg3UFrQPmk8aCn5SDL" },
                engagementId = engagementId,
                config = TyradsConfig(
                    skipInitialPages = selectedOption == options[1],
                ),
                debugMode = false
            )

            val isSuccess = Tyrads.getInstance().loginUser(userID = userIdInput.ifBlank { "6" })
            isLoadingOffers = false
            if (!isSuccess) {
                return@launch
            }
            Tyrads.getInstance().showOffers()
            lastInitializedUserId = userIdInput
            lastSelectedOption = selectedOption
            widgetReloadKey++
//            onReload()
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
            verticalArrangement = Arrangement.Center
        ) {
            Row {
                Text(
                    text = "Tyrads SDK Example",
                    modifier = modifier
                )
            }
            key(userIdInput) {
                Tyrads.getInstance().TopPremiumOffers(
                    widgetStyle = Tyrads.PremiumWidgetStyles.SLIDER_CARDS
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SimpleDropdown(
                options = options,
                selectedOption = selectedOption,
                onOptionSelected = { selectedOption = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Enter API Key") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = apiSecretInput,
                onValueChange = { apiSecretInput = it },
                label = { Text("Enter API Secret") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = encryptionKey,
                onValueChange = { encryptionKey = it },
                label = { Text("Encryption Key (optional)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = engagementId,
                onValueChange = { engagementId = it },
                label = { Text("Engagement ID (optional)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = userIdInput,
                onValueChange = { userIdInput = it },
                label = { Text("Enter User ID") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (fcmToken != null)
                TextField(
                    value = fcmToken ?: "NA",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("FCM Token") },
                    singleLine = false
                )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        handleButtonClick()
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isLoadingOffers) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
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
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                    ) {
                        Text(
                            "Copy Token",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
            }

        }
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
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text("Select Initial Pages Settings") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
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