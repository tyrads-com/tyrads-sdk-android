
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AcmoTrackingController {
    var submitting = false

    private  var repository: AcmoTrackingRepository = AcmoTrackingRepository()

    fun trackUser(activity: String) {
        if (submitting) return
        submitting = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fd = hashMapOf<String, Any>("activity" to activity)
                 repository.track(fd)
                submitting = false
            } catch (e: Exception) {
                submitting = false
            }
        }
    }
}
