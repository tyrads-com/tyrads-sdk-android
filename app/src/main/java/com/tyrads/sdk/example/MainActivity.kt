package com.tyrads.sdk.example

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
import androidx.lifecycle.lifecycleScope
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.example.ui.theme.TyradsSdkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            Tyrads.getInstance().init(
                context = this@MainActivity,
                apiKey = "4f0eaa99e38e49b8b52804116e638a41",
                apiSecret = "cd3c34a52a3b75a3fdd928774615d4e142dd2e6a8ce9da14df4205c7cc812ce81d3656e3dc2c0c58ed05c75c57f87a3431fed62725bb0286f9461521b6c9997a",
                encryptionKey = "dKWuxV#Ab9pBXNvg3UFrQPmk8aCn5SDL",
                debugMode = true
            )

            val isSuccess = Tyrads.getInstance().loginUser(userID = "4560")
            if (isSuccess) {
                Log.d("Tyrads", "Top offers loaded")
            }
        }
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

    val sharedPreferences = context.getSharedPreferences("TyradsPrefs", Context.MODE_PRIVATE)

    var apiKeyInput by remember { mutableStateOf(sharedPreferences.getString("apiKey", "") ?: "") }
    var apiSecretInput by remember {
        mutableStateOf(sharedPreferences.getString("apiSecret", "") ?: "")
    }
    var encryptionKey by remember {
        mutableStateOf(
            sharedPreferences.getString("encryptionKey", "") ?: ""
        )
    }
    var userIdInput by remember { mutableStateOf(sharedPreferences.getString("userId", "1") ?: "") }

    fun handleButtonClick() {
//        if (apiKeyInput.isBlank() || apiSecretInput.isBlank() || userIdInput.isBlank()) {
//            // Show a message to the user
//            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//            return
//        }

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
                debugMode = false
            )

            Tyrads.getInstance().loginUser(userID = userIdInput.ifBlank { "6" })
            Tyrads.getInstance().showOffers()
            isLoadingOffers = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(horizontal = 16.dp)
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
        Tyrads.getInstance().TopPremiumOffers(
            widgetStyle = Tyrads.PremiumWidgetStyles.SLIDER_CARDS
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("Enter API Key") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = apiSecretInput,
            onValueChange = { apiSecretInput = it },
            label = { Text("Enter API Secret") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = encryptionKey,
            onValueChange = { encryptionKey = it },
            label = { Text("Encryption Key (optional)") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = userIdInput,
            onValueChange = { userIdInput = it },
            label = { Text("Enter User ID") },
            singleLine = true
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
                    color = Color.White
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
