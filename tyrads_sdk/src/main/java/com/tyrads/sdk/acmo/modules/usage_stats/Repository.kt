import androidx.annotation.Keep
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.helpers.AcmoEncrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Keep
class AcmoUsageStatRepository {
    suspend fun saveUsageStats(fd: Map<String, Any>): Any = withContext(Dispatchers.IO) {
        val tyrads = Tyrads.getInstance()
        val currentEncKey = tyrads.encKey
        val encData =
            if (tyrads.isSecure && !currentEncKey.isNullOrBlank()) AcmoEncrypt(encryptionKey = currentEncKey).encryptDataAESGCM(
                data = fd
            ) else emptyMap()
        val body = Gson().toJson(if (tyrads.isSecure && !currentEncKey.isNullOrBlank()) encData else fd)
        val (_, response, result) = Fuel.post(AcmoEndpointNames.USAGE_STATS)
            .body(body)
            .response()

        when (result) {
            is Result.Success -> {
                result.value
            }

            is Result.Failure -> {
                throw result.error
            }
        }
    }

}