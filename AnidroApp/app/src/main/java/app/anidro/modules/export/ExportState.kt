package app.anidro.modules.export

import android.net.Uri

sealed class ExportState {
    object Initial : ExportState()
    data class InProgress(val totalFramesCount: Int, val currentFrameNumber: Int) : ExportState()
    object Failed : ExportState()
    object Cancelled : ExportState()
    data class Finished(val uri: Uri, val mimeType: String) : ExportState()
}