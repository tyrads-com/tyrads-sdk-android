import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.legal.CloseonTap

@Composable
fun AcmoUsagePermissionsPage() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            CloseonTap()

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Body()
                Spacer(modifier = Modifier.height(70.dp))
                UsageStatsCard(
                    onGrant = {
                        Tyrads.getInstance().preferences.edit().putBoolean(AcmoKeyNames.PRIVACY_ACCEPTED, true).apply()
                        Tyrads.getInstance().navController.navigate("webview")
                    }
                )
            }
        }
    }
}


@Composable
fun Body() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Allow App To Track Usage Data\nTo Enable Your Earning Potential",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.privacy_banner),
            contentDescription = "Privacy Banner",
            modifier = Modifier
                .height(240.dp)
                .padding(top = 25.dp)
        )
    }
}
