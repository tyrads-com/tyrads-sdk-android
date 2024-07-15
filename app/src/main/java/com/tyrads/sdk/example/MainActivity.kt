package com.tyrads.sdk.example

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
    val context = LocalContext.current // Get the current context
    var isLoading by remember { mutableStateOf(false) }

    var userIdInput by remember { mutableStateOf("") }

    fun handleButtonClick() {
        isLoading = true
        CoroutineScope(Dispatchers.Main).launch {

            Tyrads.getInstance().init(
                context,
                apiKey = BuildConfig.TYRADS_API_KEY,
                apiSecret = BuildConfig.TYRADS_API_SECRET
            )
            Tyrads.getInstance().loginUser(userID = userIdInput)
            Tyrads.getInstance().showOffers()
            Tyrads.getInstance().initializationWait?.join()
            isLoading = false
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
            if (isLoading) {
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