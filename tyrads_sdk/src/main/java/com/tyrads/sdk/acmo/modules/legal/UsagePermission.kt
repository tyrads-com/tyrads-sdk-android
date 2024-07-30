import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result


class AcmoUsagePermissionsPage(private val closeButtonOnTap: (() -> Unit)? = null) {

    @Composable
    fun Content() {
        val context = LocalContext.current

        Column {
            Text(
                text = "Usage Permissions",
                fontFamily = FontFamily.Default
            )

            // Add other UI components here

            Button(onClick = { closeButtonOnTap?.invoke() }) {
                Text("Close")
            }
        }
    }

    // Example of how you might use SharedPreferences in Kotlin
    private fun savePreference(context: Context, key: String, value: String) {
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    private fun makeNetworkCall() {
        "https://api.example.com/data".httpGet().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    println(ex)
                }
                is Result.Success -> {
                    val data = result.get()
                    println(data)
                }
            }
        }
    }

    companion object {
        fun getIntent(context: Context): Intent? {
            TODO("Not yet implemented")
        }
    }
}

@Preview
@Composable
fun PreviewAcmoUsagePermissionsPage() {
    AcmoUsagePermissionsPage().Content()
}
