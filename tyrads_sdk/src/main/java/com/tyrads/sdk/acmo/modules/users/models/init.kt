import androidx.annotation.Keep
import com.google.android.gms.common.annotation.KeepForSdkWithFieldsAndMethods
import com.google.gson.annotations.SerializedName
@Keep
data class AcmoInitModel(
    @SerializedName("data")
    val data: Data
)
@Keep
data class Data(
    @SerializedName("newRegisteredUser")
    val newRegisteredUser: Boolean = false,
    @SerializedName("user")
    val user: User,
    @SerializedName("publisherApp")
    val publisherApp: PublisherApp
)
@Keep
data class User(
    @SerializedName("publisherUserId")
    val publisherUserId: String
)
@Keep
data class PublisherApp(
    @SerializedName("headerColor")
    val headerColor: String = "",
    @SerializedName("mainColor")
    val mainColor: String = ""
)
