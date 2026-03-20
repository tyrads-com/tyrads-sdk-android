package com.tyrads.sdk.acmo.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.google.android.gms.appset.AppSet
import com.google.android.gms.tasks.Tasks
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig

@Keep
object ExtraDeviceDetails {

    fun getGpuInfo(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL
            } else {
                val glRenderer = getOpenGLRenderer()

                if (glRenderer != null && glRenderer.isNotEmpty() && glRenderer != "Unknown") {
                    glRenderer
                } else {
                    getGpuFromSystemProperty()
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getOpenGLRenderer(): String? {
        return try {
            val egl = javax.microedition.khronos.egl.EGLContext.getEGL() as? EGL10

            if (egl == null) {
                return null
            }

            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            if (display == EGL10.EGL_NO_DISPLAY) {
                return null
            }

            val versionInfo = IntArray(2)
            if (!egl.eglInitialize(display, versionInfo)) {
                return null
            }

            val configAttribs = intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
            )

            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)

            if (!egl.eglChooseConfig(display, configAttribs, configs, 1, numConfigs)) {
                egl.eglTerminate(display)
                return null
            }

            if (configs[0] == null || numConfigs[0] == 0) {
                egl.eglTerminate(display)
                return null
            }

            val contextAttribs = intArrayOf(
                0x3098,
                2,
                EGL10.EGL_NONE
            )

            val context = egl.eglCreateContext(
                display,
                configs[0],
                EGL10.EGL_NO_CONTEXT,
                contextAttribs
            )

            if (context == EGL10.EGL_NO_CONTEXT) {
                egl.eglTerminate(display)
                return null
            }

            val surfaceAttribs = intArrayOf(
                EGL10.EGL_WIDTH, 1,
                EGL10.EGL_HEIGHT, 1,
                EGL10.EGL_NONE
            )

            val surface = egl.eglCreatePbufferSurface(display, configs[0], surfaceAttribs)

            if (surface == EGL10.EGL_NO_SURFACE) {
                egl.eglDestroyContext(display, context)
                egl.eglTerminate(display)
                return null
            }

            egl.eglMakeCurrent(display, surface, surface, context)

            val renderer = android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_RENDERER)
            val vendor = android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_VENDOR)
            val glVersion = android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_VERSION)

            egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
            egl.eglDestroySurface(display, surface)
            egl.eglDestroyContext(display, context)
            egl.eglTerminate(display)

            when {
                renderer != null && renderer.isNotEmpty() -> renderer
                vendor != null && vendor.isNotEmpty() -> vendor
                glVersion != null && glVersion.isNotEmpty() -> glVersion
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getGpuFromSystemProperty(): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop ro.hardware.gles")
            val reader = process.inputStream.bufferedReader()
            val result = reader.readLine() ?: "Unknown"
            process.destroy()
            result
        } catch (e: Exception) {
            try {
                val process = Runtime.getRuntime().exec("getprop ro.opengles.version")
                val reader = process.inputStream.bufferedReader()
                val result = reader.readLine() ?: "Unknown"
                process.destroy()
                result
            } catch (e2: Exception) {
                "Unknown"
            }
        }
    }

    fun isBluetoothSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    fun isBluetoothLESupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun getBluetoothInfo(context: Context): String {
        return when {
            isBluetoothLESupported(context) -> "BLE_SUPPORTED"
            isBluetoothSupported(context) -> "CLASSIC_SUPPORTED"
            else -> "NOT_SUPPORTED"
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("UNUSED")
    fun getBluetoothAdapterName(context: Context? = null): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }
            } else if (context != null) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }
            }

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.name
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun getBluetoothInfoDetailed(context: Context): Map<String, Any?> {
        return mapOf(
            "bluetooth_capability" to getBluetoothInfo(context),
            "bluetooth_supported" to isBluetoothSupported(context),
            "bluetooth_le_supported" to isBluetoothLESupported(context),
            "bluetooth_adapter_name" to getBluetoothAdapterName(context)
        )
    }

    @Suppress("UNUSED")
    fun isTouchScreenSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }

    fun getTouchSupportInfo(context: Context): Map<String, Boolean> {
        return mapOf(
            "touchscreen" to context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN),
            "touchscreen_multitouch" to context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH),
            "touchscreen_multitouch_distinct" to context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT),
            "touchscreen_multitouch_jazzhand" to context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND),
            "touchscreen_pressure" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                try {
                    context.packageManager.hasSystemFeature("android.hardware.touchscreen.multitouch.jazzhand")
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        )
    }

    fun getTouchSupportString(context: Context): String {
        val supportInfo = getTouchSupportInfo(context)

        return when {
            supportInfo["touchscreen_multitouch_jazzhand"] == true -> "MULTITOUCH_JAZZHAND"
            supportInfo["touchscreen_multitouch_distinct"] == true -> "MULTITOUCH_DISTINCT"
            supportInfo["touchscreen_multitouch"] == true -> "MULTITOUCH"
            supportInfo["touchscreen"] == true -> "SINGLE_TOUCH"
            else -> "NOT_SUPPORTED"
        }
    }

    @Suppress("UNUSED")
    fun getGoogleAppSetId(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val appSetIdClient = AppSet.getClient(context)
            val appSetIdTask = appSetIdClient.appSetIdInfo

            appSetIdTask.addOnSuccessListener { appSetIdInfo ->
                val appSetId = appSetIdInfo.id
                onSuccess(appSetId)
            }.addOnFailureListener { exception ->
                onError(exception)
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun getGoogleAppSetIdSync(context: Context): String? {
        return try {
            val appSetIdClient = AppSet.getClient(context)
            val appSetIdTask = appSetIdClient.appSetIdInfo

            val appSetIdInfo = Tasks.await(appSetIdTask)
            appSetIdInfo.id
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNUSED")
    fun setupMotionEventListener(view: View) {
        view.setOnTouchListener { _, event ->
            handleMotionEvent(event)
            false
        }
    }

    private fun handleMotionEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1) {
                    EventTracker.incrementClickEvent()
                }
                EventTracker.incrementTouchEvent()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                EventTracker.incrementTouchEvent()
            }
            MotionEvent.ACTION_MOVE -> {
                EventTracker.incrementMouseEvent()
            }
            MotionEvent.ACTION_UP -> {
                EventTracker.incrementTouchEvent()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                EventTracker.incrementTouchEvent()
            }
            MotionEvent.ACTION_CANCEL -> {
                EventTracker.incrementTouchEvent()
            }
        }
    }

    fun getExtraDeviceDetailsMap(context: Context): Map<String, Any?> {
        val touchSupportInfo = getTouchSupportInfo(context)
        val bluetoothInfo = getBluetoothInfoDetailed(context)

        val keyboardScore = KeyboardTracker.calculateKeyboardScore()
        val clipboardScore = ClipboardTracker.calculateClipboardScore()

        return mapOf(
            "gpu" to getGpuInfo(),
            "bluetooth" to getBluetoothInfo(context),
            "bluetooth_supported" to isBluetoothSupported(context),
            "bluetooth_le_supported" to isBluetoothLESupported(context),
            "bluetooth_adapter_name" to bluetoothInfo["bluetooth_adapter_name"],
            "touch_support" to getTouchSupportString(context),
            "touch_multitouch" to (touchSupportInfo["touchscreen_multitouch"] ?: false),
            "touch_multitouch_distinct" to (touchSupportInfo["touchscreen_multitouch_distinct"] ?: false),
            "touch_multitouch_jazzhand" to (touchSupportInfo["touchscreen_multitouch_jazzhand"] ?: false),
            "touch_pressure" to (touchSupportInfo["touchscreen_pressure"] ?: false),
            "touch_num_events" to EventTracker.getTouchEventCount(),
            "click_num_events" to EventTracker.getClickEventCount(),
            "mouse_num_events" to EventTracker.getMouseEventCount(),
            "clipboard_num_events" to ClipboardTracker.getClipboardEventCount(),
            "clipboard_score" to clipboardScore,
            "keyboard_num_events" to KeyboardTracker.getKeyboardEventCount(),
            "keyboard_score" to keyboardScore
        )
    }

    @SuppressLint("ServiceCast")
    @Suppress("UNUSED")
    fun setupKeyboardEventListener(context: Context) {
        try {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

            if (inputMethodManager != null) {
                KeyboardTracker.incrementKeyboardEvent()
            }
        } catch (e: Exception) {
        }
    }
}