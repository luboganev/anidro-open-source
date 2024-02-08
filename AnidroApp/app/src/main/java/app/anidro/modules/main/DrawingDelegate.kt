package app.anidro.modules.main

import android.graphics.Canvas

/**
 * This interface defines the drawing delegate which is needed by the
 * [app.anidro.modules.main.views.DrawingView] and should contain
 * all the logic for performing drawing manipulations and storing the current
 * drawing
 */
interface DrawingDelegate {
    /**
     * Method to be called on each touch event while drawing
     *
     * @param touchX
     * The x of the touch event
     * @param touchY
     * The y of the touch event
     * @param touchActionMasked
     * The action of the touch event
     * @return
     * Whether this is a valid drawing event which should be processed
     */
    fun onTouchEvent(touchX: Float, touchY: Float, touchActionMasked: Int): Boolean

    /**
     * Redraws the current drawing using the provided canvas and path
     */
    fun redrawCurrentDrawing(drawCanvas: Canvas?)

    /**
     * Redraws only the background of the current drawing using the provided canvas
     */
    fun redrawBackground(drawCanvas: Canvas?)

    /**
     * Returns the color of the current brush
     */
    val brushColor: Int

    /**
     * Returns stroke width of the current brush in DP
     */
    val brushStrokeWidth: Int
}