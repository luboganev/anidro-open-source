package app.anidro.common

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

/**
 * A helper class which can convert DP to PX units,
 * depending on the current device's display metrics
 */
class DisplayMetricsConverter(resources: Resources) {

    private val displayMetrics: DisplayMetrics = resources.displayMetrics

    /**
     * Convert the input DP value into physical device pixels
     */
    fun pixelsFromDp(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(), displayMetrics)
    }
}