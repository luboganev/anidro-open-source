package app.anidro.modules.main

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.lifecycle.LifecycleOwner
import app.anidro.models.TimedSegment

interface MainPresenterOutput : LifecycleOwner {
    fun attachDrawingDelegate(drawingDelegate: DrawingDelegate)
    fun showAcceptTermsDialog()
    fun updateDrawingUIVisibility(isDrawing: Boolean, isDrawingEmpty: Boolean, animate: Boolean)
    fun updatePresentationUIVisibility(isPresenting: Boolean, animate: Boolean,
                                       segments: List<TimedSegment>, @ColorInt backgroundColor: Int)
    fun updateExportUIVisibility(isExporting: Boolean, animate: Boolean)
    fun notifyDrawingCleared()
    fun notifyLastDrawingSegmentRemoved(isDrawingEmpty: Boolean)
    fun updateDrawingTimeWarning(drawingTimeProgress: Int, animate: Boolean)
    fun showDrawingTimeExceeded()
    fun showUIAfterDrawingStops(immediately: Boolean)
    fun hideUIWhenDrawingStarts()
    fun updateExportProgressView(frame: Bitmap)
    fun updateExportProgressPercent(exportProgressPercent: Int)
    fun showExportFailedMessage()
    fun showSelectBrushUI(size: Int, colorIndex: Int, lightnessIndex: Int)
    fun notifyDrawingBrushSelected()
    fun showSelectBackgroundColorUI(colorIndex: Int, lightnessIndex: Int)
    fun notifyDrawingBackgroundSelected()
}