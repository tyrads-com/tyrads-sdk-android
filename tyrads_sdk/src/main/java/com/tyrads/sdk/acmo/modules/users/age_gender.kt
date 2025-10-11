import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import android.widget.Toast
import android.util.Log
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.extensions.toColor
import com.tyrads.sdk.acmo.modules.users.components.AcmoComponentGenderSelector
import com.tyrads.sdk.acmo.modules.users.components.AcmoComponentAgeSelector
import com.tyrads.sdk.acmo.core.services.LocalizationService
import com.tyrads.sdk.acmo.modules.users.AcmoUsersController
import kotlinx.coroutines.launch

@Composable
fun AcmoUsersUpdatePage(
    onComplete: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    returnToWidget: Boolean? = false
) {
    var selectedGender by remember { mutableStateOf<Int?>(null) }
    var selectedAge by remember { mutableStateOf(18) }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val usersController = remember { AcmoUsersController() }
    val localizationService = LocalizationService.getInstance()

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(113.dp))

                Text(
                    text = localizationService.translate("data.initialization.userInfo.title"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Tyrads.getInstance().mainColor?.toColor() ?: Color(0xFF2CB388),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(80.dp))

                Text(
                    text = localizationService.translate("data.initialization.userInfo.chooseGender.label"),
                    style = TextStyle(
                        color = Tyrads.getInstance().mainColor?.toColor() ?: Color(0xFF2CB388),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier.height(120.dp)
                ) {
                    AcmoComponentGenderSelector(
                        selectedGender = selectedGender,
                        onGenderSelected = { selectedGender = it }
                    )
                }

                Spacer(modifier = Modifier.height(70.dp))

                Text(
                    text = localizationService.translate("data.initialization.userInfo.chooseAge.label"),
                    style = TextStyle(
                        color = Tyrads.getInstance().mainColor?.toColor() ?: Color(0xFF2CB388),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                AcmoComponentAgeSelector(
                    onChanged = { selectedAge = it },
                    init = selectedAge,
                    min = 13
                )

                Spacer(modifier = Modifier.height(60.dp))

                Button(
                    onClick = {
                        if (!isSubmitting) {
                            if (selectedGender == null) {
                                Toast.makeText(
                                    context,
                                    "Please select gender and age to proceed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isSubmitting = true
                            coroutineScope.launch {
                                try {
                                    usersController.updateUser(
                                        userId = Tyrads.getInstance().publisherUserID!!,
                                        age = selectedAge,
                                        gender = selectedGender!!
                                    )
                                    if (returnToWidget == true) {
                                        onComplete?.invoke()
                                    } else {
                                        (context as? ComponentActivity)?.finish()
                                        Tyrads.getInstance().showOffers()
                                    }

                                } catch (e: Exception) {
                                    Log.e("AcmoUsersUpdate", "Error updating user: ${e.message}")
                                    Toast.makeText(
                                        context,
                                        "Error updating profile. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(50.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Tyrads.getInstance().mainColor?.toColor() ?: Color(0xFF2CB388)
                    ),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = localizationService.translate("data.initialization.userInfo.cta.continue"),
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AcmoUsersUpdatePagePreview() {
    MaterialTheme {
        AcmoUsersUpdatePage()
    }
}