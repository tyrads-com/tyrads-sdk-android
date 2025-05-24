import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import com.tyrads.sdk.R
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.modules.users.components.AcmoComponentGenderSelector
import com.tyrads.sdk.acmo.modules.users.components.AcmoComponentAgeSelector


@Composable
fun AcmoUsersUpdatePage() {
    var selectedGender by remember { mutableStateOf<Int?>(null) }
    var selectedAge by remember { mutableStateOf(18) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.singup_bg),
                contentDescription = "Background",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
             Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CloseonTap()

                Spacer(modifier = Modifier.height(65.dp))

                // Title section
                Text(
                    text = stringResource(id = R.string.user_profile_title, "User Profile"),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF2CB388),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(80.dp))

                // Gender section
                Text(
                    text = stringResource(id = R.string.gender_title, "Your Gender"),
                    style = TextStyle(
//                        color = MaterialTheme.colorScheme.secondary,
                        color = Color(0xFF2CB388),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )


                Spacer(modifier = Modifier.height(30.dp))

                // Gender selection component
                Box(
                    modifier = Modifier.height(120.dp)
                ) {
                    AcmoComponentGenderSelector(
                        selectedGender = selectedGender,
                        onGenderSelected = { selectedGender = it }
                    )
                }

                Spacer(modifier = Modifier.height(70.dp))
                // Age section
                Text(
                    text = stringResource(id = R.string.age_title, "Your Age"),
                    style = TextStyle(
//                        color = MaterialTheme.colorScheme.secondary,
                        color = Color(0xFF2CB388),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))
                // Age selection component
                AcmoComponentAgeSelector(
                    onChanged = { selectedAge = it },
                    init = selectedAge,
                    min = 13
                )

                Spacer(modifier = Modifier.height(60.dp))

                Button(
                    onClick = {
                        if (!isSubmitting) {
                            isSubmitting = true

                            Tyrads.getInstance().navController.navigate("offers") {
                                popUpTo(Tyrads.getInstance().navController.graph.startDestinationId) {
                                    inclusive = true
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
                        containerColor = Color(0xFF2CB388)
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
                            text = stringResource(id = R.string.continue_button, "Continue"),
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

@Composable
fun CloseonTap() {
    val activityContext = LocalContext.current as? ComponentActivity
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(end = 10.dp)
    ) {
        IconButton(
            onClick = {
                activityContext?.finish()
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFFC4C4C4)
            )
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
