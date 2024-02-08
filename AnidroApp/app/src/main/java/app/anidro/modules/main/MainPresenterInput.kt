package app.anidro.modules.main

import app.anidro.common.ScreenOrientationSensor.ScreenOrientation
import app.anidro.models.FileType
import app.anidro.modules.main.terms.AcceptTermsCallback

interface MainPresenterInput : AcceptTermsCallback {
    fun setView(view: MainPresenterOutput)
    fun onViewShow()
    fun onViewHide()
    fun onDestroy()
    fun onBackPressed(): Boolean
    fun onPlayButtonClicked()
    fun onSettingsButtonClicked()
    fun onActionBackClicked()
    fun onActionBrushClicked()
    fun onActionBackgroundColorClicked()
    fun onActionDeleteClicked()
    fun onActionUndoClicked()
    fun onAnimationClicked()
    fun onShareFileTypeSelected(fileType: FileType?)
    fun onCancelExportClicked()
    fun onBrushSelected(size: Int, colorIndex: Int, lightnessIndex: Int)
    fun onBackgroundColorSelected(colorIndex: Int, lightnessIndex: Int)
    fun onCanvasChanged(width: Int, height: Int, screenOrientation: ScreenOrientation)
}