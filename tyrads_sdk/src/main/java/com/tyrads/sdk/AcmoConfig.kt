import android.graphics.Color

object AcmoConfig {
   const val API_VERSION = "3.0";
    // MAJOR_VERSION.MINOR_VERSION.PATCH_VERSION-BUILD_VERSION
    // EX: 2.3.0-1
    // MAJOR Version (X.0.0-0)
    // MINOR Version (0.X.0-0)
    // PATCH Version (0.0.X-0)
    // BUILD Version (0.0.0-X)
   const val MAJOR_VERSION = "3";
   const val MINOR_VERSION = "2";
   const val BUILD_VERSION = "0";
   const val PATCH_VERSION = "1";

   const val SDK_VERSION = "$MAJOR_VERSION.$MINOR_VERSION.$PATCH_VERSION-$BUILD_VERSION";
   const val SDK_PLATFORM = "React Native";
   const val BASE_URL = "https://api.tyrads.com/v$API_VERSION/";
    const val TAG = "TyrAds SDK"

    val PRIMARY_COLOR = Color.rgb(0, 36, 51)
    val PRIMARY_COLOR_LIGHT = Color.rgb(153, 145, 145)
    val PRIMARY_COLOR_DARK = Color.rgb(0, 0, 0)
    
    val SECONDARY_COLOR = Color.rgb(44, 179, 136)
    val SECONDARY_COLOR_LIGHT = Color.rgb(203, 235, 207)
    val SIDEBAR_BACKGROUND_COLOR_LIGHT = Color.argb(138, 255, 255, 255) // Approximation of Colors.white54
    val SIDEBAR_BACKGROUND_COLOR_DARK = Color.rgb(17, 45, 30)
    val APPBAR_BG = Color.rgb(0, 33, 48)
    
    // Note: ThemeMode.light doesn't have a direct equivalent in Android
    // You would typically handle this in your app's theme configuration
}