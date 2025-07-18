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
        val encData =
            if (Tyrads.getInstance().isSecure) AcmoEncrypt(encryptionKey = Tyrads.getInstance().encKey!!).encryptDataAESGCM(
                data = fd
            ) else emptyMap()
        val body = Gson().toJson(if (Tyrads.getInstance().isSecure) encData else fd)
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