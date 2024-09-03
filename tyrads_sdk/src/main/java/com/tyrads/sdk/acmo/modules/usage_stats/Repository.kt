import android.util.Log
import androidx.annotation.Keep
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
@Keep
class AcmoUsageStatRepository {
    suspend fun saveUsageStats(fd: Map<String, Any>): Any = withContext(Dispatchers.IO) {
        val body = Gson().toJson(fd)
        val (_, response, result) = Fuel.post( AcmoEndpointNames.USAGE_STATS)
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