package app.anidro.common

import android.content.res.Resources
import android.graphics.Paint

/**
 * This class provides methods for building a [Paint]
 * used for drawing stuff on the screen
 */
object DrawingPaintBuilder {
    /**
     * The default stroke width of the paint is 8dp
     */
    const val DEFAULT_STROKE_WIDTH_DP = 8

    /**
     * Returns a new {link Paint} instance with the input color and stroke width.
     * The [Resources] are needed to be able to convert from DP to PX.
     */
    @JvmStatic
    fun getPaint(color: Int, res: Resources, strokeWidthDP: Int): Paint {
        val displayMetricsConverter = DisplayMetricsConverter(res)
        return getPaint(color, displayMetricsConverter.pixelsFromDp(strokeWidthDP))
    }

    /**
     * Returns a new {link Paint} instance with the input color and stroke width
     */
    @JvmStatic
    fun getPaint(color: Int, strokeWidth: Float): Paint {
        val paint = Paint()
        paint.color = color
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        return paint
    }
}