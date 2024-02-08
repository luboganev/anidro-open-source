package app.anidro.common

import android.content.Context
import android.view.Surface
import android.view.WindowManager

/**
 * This class defines all possible screen orientations and
 * contains helper method for getting the current one.
 */
object ScreenOrientationSensor {
    /**
     * Return the orientation of the current previewing screen
     *
     * @param context
     * We need the context to get the orientation
     * @return The [ScreenOrientation]
     */
    @JvmStatic
    fun getOrientation(context: Context): ScreenOrientation {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val disp = wm.defaultDisplay
        val rot = disp.rotation
        return when (rot) {
            Surface.ROTATION_0 -> ScreenOrientation.ORIENTATION_0
            Surface.ROTATION_90 -> ScreenOrientation.ORIENTATION_90
            Surface.ROTATION_180 -> ScreenOrientation.ORIENTATION_180
            Surface.ROTATION_270 -> ScreenOrientation.ORIENTATION_270
            else -> ScreenOrientation.ORIENTATION_0
        }
    }

    /**
     * An enum containing all possible screen orientations,
     * where [ORIENTATION_0] is the natural device orientation and
     * all other orientations are rotated relative to the natural one.
     */
    enum class ScreenOrientation {
        ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270
    }
}