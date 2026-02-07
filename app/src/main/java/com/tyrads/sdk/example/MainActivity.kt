package com.tyrads.sdk.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isLoadingOffers by remember { mutableStateOf(false) }
    var isLoadingWidgets by remember { mutableStateOf(false) }
    var isTyradsInitialized by remember { mutableStateOf(false) }

    val sharedPreferences = context.getSharedPreferences("TyradsPrefs", Context.MODE_PRIVATE)

    var apiKeyInput by remember { mutableStateOf(sharedPreferences.getString("apiKey", "") ?: "") }
    var apiSecretInput by remember {
        mutableStateOf(sharedPreferences.getString("apiSecret", "") ?: "")
    }
    var userIdInput by remember { mutableStateOf(sharedPreferences.getString("userId", "1322") ?: "") }

    fun handleButtonClick() {
        isLoadingOffers = true
        CoroutineScope(Dispatchers.Main).launch {
            sharedPreferences.edit().apply {
                putString("apiKey", apiKeyInput)
                putString("apiSecret", apiSecretInput)
                putString("userId", userIdInput)
                apply()
            }

            // ✅ ONLY CHANGE: Removed init() and loginUser() calls here
            // Just show offers directly
            Tyrads.getInstance().showOffers()
            isLoadingOffers = false
        }
    }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            Tyrads.getInstance().init(
                context,
                apiKey = "0a55de10c58f459c9f65988d9d33e774",
                apiSecret = "418fc08c18a6715b48428568946e6f82f0ff06bfbc017944d22a19b3317a5ce2ad7028b0599a149534d957017d54650a9fa355cebf6971d7fdbc3eca372ca4ed",
                debugMode = true,
                config = TyradsConfig(
                    skipInitialPages = true
                )
            )
            val userData = Tyrads.getInstance().loginUser(userID = "78y86")
            isTyradsInitialized = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
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
        if (isTyradsInitialized) {
            Tyrads.getInstance().TopPremiumOffers(widgetStyle = Tyrads.PremiumWidgetStyles.SLIDER_CARDS)
        } else {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("Enter API Key") },
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = apiSecretInput,
            onValueChange = { apiSecretInput = it },
            label = { Text("Enter API Secret") },
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = userIdInput,
            onValueChange = { userIdInput = it },
            label = { Text("Enter User ID") },
            modifier = Modifier.padding(16.dp)
        )

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
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = "Show Offers")
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