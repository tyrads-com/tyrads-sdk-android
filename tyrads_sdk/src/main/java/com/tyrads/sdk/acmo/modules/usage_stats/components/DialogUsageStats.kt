import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AcmoUsageStatsDialog(
    dismissible: Boolean,
    onDismissRequest: () -> Unit
) {
    val usageStatsController = AcmoUsageStatsController()
    var checked by remember { mutableStateOf(false) }
    var usagePermissionsLoader by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                CoroutineScope(Dispatchers.Main).launch {
                    val permissionStatus = usageStatsController.checkUsagePermission()
                    delay(500) // 1 second delay
                    usagePermissionsLoader = false
                    checked = permissionStatus ?: false
                    if (permissionStatus) {
                        onDismissRequest()
                    }

                }

            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .height(160.dp)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Permit Usage Access",
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 48.dp)
                    ) {
                        if (usagePermissionsLoader) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.Center)
                            )
                        } else {
                            Switch(
                                checked = checked,
                                onCheckedChange = {
                                    checked = true
                                    val permissionStatus =
                                        usageStatsController.checkUsagePermission()
                                    if (!permissionStatus) {
                                        usageStatsController.grantUsagePermission()
                                        usagePermissionsLoader = true
                                    } else {
                                      //  onDismissRequest()
                                    }


                                },
                                colors = SwitchDefaults.colors(
//                                    checkedThumbColor = MaterialTheme.colors.secondary,
//                                    uncheckedThumbColor = MaterialTheme.colors.secondary,
                                    checkedTrackColor = Color.Black.copy(alpha = 0.12f),
                                    uncheckedTrackColor = Color.Black.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
                Text(
                    text = "In order to reward your app usage, please give permission to this App in the preferences",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}