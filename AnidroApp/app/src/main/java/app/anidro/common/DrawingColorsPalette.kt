package app.anidro.common

import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.ColorInt
import app.anidro.R

/**
 * This class represents the drawing color palette.
 * It contains the indexes of the default colors as well as
 * getters for all supported drawing colors. Each color is
 * uniquely defined by its color index and its lightness index.
 */
class DrawingColorsPalette(private val resources: Resources) {

    /**
     * Returns the default drawing color, i.e. pitch black
     */
    @ColorInt
    fun getDefaultDrawingColor(): Int {
        return getColorWithLightness(DEFAULT_BRUSH_COLOR_INDEX, DEFAULT_BRUSH_COLOR_LIGHTNESS_INDEX)
    }

    /**
     * Returns the default background color, i.e. pure white
     */
    @ColorInt
    fun getDefaultBackgroundColor(): Int {
        return getColorWithLightness(DEFAULT_BACKGROUND_COLOR_INDEX, DEFAULT_BACKGROUND_COLOR_LIGHTNESS_INDEX)
    }

    /**
     * Returns an array containing all colors for a particular lightness
     */
    @ColorInt
    fun getColorsForLightness(lightnessIndex: Int): IntArray {
        var colorLightnesses: TypedArray
        val allColors = resources.obtainTypedArray(R.array.drawing_colors)
        @ColorInt val lightnesscolors = IntArray(allColors.length())
        var i = 0
        val size = allColors.length()
        while (i < size) {
            colorLightnesses = resources.obtainTypedArray(
                    allColors.getResourceId(i, R.array.drawing_color_grey))
            lightnesscolors[i] = colorLightnesses.getColor(lightnessIndex, Color.BLACK)
            colorLightnesses.recycle()
            i++
        }
        allColors.recycle()
        return lightnesscolors
    }

    /**
     * Returns an array containing all lightnesses for a particular color
     */
    @ColorInt
    fun getLightnessesForColor(colorIndex: Int): IntArray {
        val allColors = resources.obtainTypedArray(R.array.drawing_colors)
        val colorLightnesses = resources.obtainTypedArray(
                allColors.getResourceId(colorIndex, R.array.drawing_color_grey))
        allColors.recycle()
        @ColorInt val colors = IntArray(colorLightnesses.length())
        var i = 0
        val size = colorLightnesses.length()
        while (i < size) {
            colors[i] = colorLightnesses.getColor(i, Color.BLACK)
            i++
        }
        colorLightnesses.recycle()
        return colors
    }

    /**
     * Returns a particular color defined by its color index and lightness index
     */
    @ColorInt
    fun getColorWithLightness(colorIndex: Int, lightnessIndex: Int): Int {
        val allColors = resources.obtainTypedArray(R.array.drawing_colors)
        val colorLightnesses = resources.obtainTypedArray(
                allColors.getResourceId(colorIndex, R.array.drawing_color_grey))
        @ColorInt val colorForLightness = colorLightnesses.getColor(lightnessIndex, Color.BLACK)
        colorLightnesses.recycle()
        allColors.recycle()
        return colorForLightness
    }

    companion object {
        /**
         * Default drawing color is grey
         */
        const val DEFAULT_BRUSH_COLOR_INDEX = 11

        /**
         * Default drawing color lightness is the darkest possible, i.e. pitch black
         */
        const val DEFAULT_BRUSH_COLOR_LIGHTNESS_INDEX = 4

        /**
         * Default background color is grey
         */
        const val DEFAULT_BACKGROUND_COLOR_INDEX = 11

        /**
         * Default background color lightness is the lightest possible, i.e. pure white
         */
        const val DEFAULT_BACKGROUND_COLOR_LIGHTNESS_INDEX = 0

        /**
         * The lightness index of each base color. This color is neither too bright nor too
         * dark, but just right. It is also in the middle of the available lightness spectrum for each
         * of drawing colors.
         */
        const val BASE_COLOR_LIGHTNESS_INDEX = 2
    }
}