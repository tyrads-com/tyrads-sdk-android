package com.tyrads.sdk.example

import LoadingOverlay
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.example.ui.theme.TyradsSdkTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
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

    val sharedPreferences = context.getSharedPreferences("TyradsPrefs", Context.MODE_PRIVATE)

    var apiKeyInput by remember { mutableStateOf(sharedPreferences.getString("apiKey", "") ?: "") }
    var apiSecretInput by remember {
        mutableStateOf(sharedPreferences.getString("apiSecret", "") ?: "")
    }
    var userIdInput by remember { mutableStateOf(sharedPreferences.getString("userId", "") ?: "") }

    fun handleButtonClick() {
        if (apiKeyInput.isBlank() || apiSecretInput.isBlank() || userIdInput.isBlank()) {
            // Show a message to the user
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoadingOffers = true
        CoroutineScope(Dispatchers.Main).launch {
            sharedPreferences.edit().apply {
                putString("apiKey", apiKeyInput)
                putString("apiSecret", apiSecretInput)
                putString("userId", userIdInput)
                apply()
            }

            Tyrads.getInstance().init(
                context,
                apiKey = apiKeyInput,
                apiSecret = apiSecretInput,
                debugMode = true
            )

            Tyrads.getInstance().loginUser(userID = userIdInput)
            Tyrads.getInstance().showOffers()
            isLoadingOffers = false
        }
    }

    fun handleWidgetsClick() {
        if (apiKeyInput.isBlank() || apiSecretInput.isBlank() || userIdInput.isBlank()) {
            // Show a message to the user
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        isLoadingWidgets = true
        CoroutineScope(Dispatchers.Main).launch {
            sharedPreferences.edit().apply {
                putString("apiKey", apiKeyInput)
                putString("apiSecret", apiSecretInput)
                putString("userId", userIdInput)
                apply()
            }

            Tyrads.getInstance().init(
                context,
                apiKey = apiKeyInput,
                apiSecret = apiSecretInput,
                debugMode = true
            )

            Tyrads.getInstance().loginUser(userID = userIdInput)
            context.startActivity(Intent(context, PremiumLayoutsActivity::class.java))
            isLoadingWidgets = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Row {
            Text(
                text = "Tyrads SDK Example",
                modifier = modifier
            )
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

        Button(
            onClick = {
                handleWidgetsClick()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            if (isLoadingWidgets) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = "Widgets")
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
