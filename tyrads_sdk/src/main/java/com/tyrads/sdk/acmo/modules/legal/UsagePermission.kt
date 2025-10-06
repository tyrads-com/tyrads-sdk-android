import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.legal.CloseonTap
import com.tyrads.sdk.acmo.core.services.LocalizationService
import kotlinx.coroutines.launch
import androidx.core.content.edit

@Composable
fun AcmoUsagePermissionsPage(
    onGrantClicked: (() -> Unit)? = null,
    returnToWidget: Boolean? = false
) {
    // Initialize LocalizationService similar to Flutter implementation
    val localizationService = LocalizationService.getInstance()

    Scaffold (
        containerColor = Color.White
    ){ innerPadding ->
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
                Body(localizationService)
                Spacer(modifier = Modifier.height(40.dp))
                UsageStatsCard(
                    onGrant = {
                        // Save privacy acceptance preference
                        Tyrads.getInstance().setPrivacyAccepted(true)

                        // Save usage stats
                        Tyrads.getInstance().tyradScope.launch {
                            val usageStatsController = AcmoUsageStatsController()
                            usageStatsController.saveUsageStats()
                        }

                        if(returnToWidget == true){
                            return@UsageStatsCard
                        }

                        val destination = if (Tyrads.getInstance().newUser) {
                            "users-update"
                        } else {
                            "webview"
                        }

                        Tyrads.getInstance().navController.navigate(destination) {
                            popUpTo(Tyrads.getInstance().navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Body(localizationService: LocalizationService) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Title using localization - matching Flutter implementation
        Text(
            text = localizationService.translate("data.initialization.usagePermission.title"),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )

        // Privacy banner image - matching Flutter sizing
        Image(
            painter = painterResource(id = R.drawable.privacy_banner),
            contentDescription = "Privacy Banner",
            modifier = Modifier
                .height(240.dp)
                .fillMaxWidth()
                .padding(top = 25.dp)
        )
    }
}