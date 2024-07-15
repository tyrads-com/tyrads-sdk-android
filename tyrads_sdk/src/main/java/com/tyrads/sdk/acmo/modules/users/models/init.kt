import com.google.gson.annotations.SerializedName

data class AcmoInitModel(
    val data: Data
)

data class Data(
    @SerializedName("newRegisteredUser")
    val newRegisteredUser: Boolean = false,
    val user: User,
    val publisherApp: PublisherApp
)

data class User(
    val publisherUserId: String
)

data class PublisherApp(
    val headerColor: String = "",
    val mainColor: String = ""
)

