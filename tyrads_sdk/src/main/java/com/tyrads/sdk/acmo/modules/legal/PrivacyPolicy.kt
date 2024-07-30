import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.tyrads.sdk.R
import com.tyrads.sdk.WebViewComposable
import com.tyrads.sdk.ui.theme.TyradsSdkTheme

class AcmoPrivacyPolicyPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        enableEdgeToEdge()
        setContent {
            TyradsSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AcmoPrivacyPolicyContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun AcmoPrivacyPolicyContent(modifier : Modifier) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
         Image(
             painter = painterResource(id = R.drawable.privacy_banner),
             contentDescription = "Logo",
             modifier = Modifier.height(48.dp)
         )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Privacy Policy",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        val annotatedString = buildAnnotatedString {
            append("By using our app, you agree to our ")
            pushStringAnnotation(
                tag = "privacy_policy",
                annotation = "https://www.example.com/privacy-policy"
            )
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                )
            ) {
                append("Privacy Policy")
            }
            pop()
            append(" and ")
            pushStringAnnotation(
                tag = "terms_of_service",
                annotation = "https://www.example.com/terms-of-service"
            )
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                )
            ) {
                append("Terms of Service")
            }
            pop()
            append(".")
        }

        Text(
            text = annotatedString,
            fontSize = 14.sp,
//            modifier = Modifier.clickable {
//                annotatedString.getStringAnnotations(it.startOffset, it.endOffset)
//                    .firstOrNull()?.let { annotation ->
//                        if (annotation.tag == "privacy_policy") {
//
//                        } else if (annotation.tag == "terms_of_service") {
//                        }
//                    }
//            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Effective Date: May 15, 2023",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Tyrads SDK Version: ${AcmoConfig.SDK_VERSION}",
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Usage Permissions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    context.startActivity(AcmoUsagePermissionsPage.getIntent(context))
                }
                .padding(8.dp)
        )
    }
}

