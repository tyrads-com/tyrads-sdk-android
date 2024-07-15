import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.tyrads.sdk.example.MainActivity
import com.tyrads.sdk.example.Greeting
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greetingDisplaysCorrectText() {
        val name = "John"
        composeTestRule.setContent {
            Greeting(name)
        }

        composeTestRule.onNodeWithText("Hello $name!").assertExists()
    }

    @Test
    fun greetingDisplaysEmptyStringWhenNameIsEmpty() {
        val name = ""
        composeTestRule.setContent {
            Greeting(name)
        }

        composeTestRule.onNodeWithText("Hello !").assertExists()
    }

    @Test
    fun greetingDisplaysNullWhenNameIsNull() {
        val name = null
        composeTestRule.setContent {
            Greeting(name ?: "")
        }

        composeTestRule.onNodeWithText("Hello !").assertExists()
    }
}
