package app.anidro.modules.export

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.anidro.models.FileType
import app.anidro.models.TimedSegment
import app.anidro.modules.export.files.DrawingsFileHelper
import app.anidro.modules.export.writers.ExportFileWriter
import app.anidro.modules.export.writers.ExportGifWriter
import app.anidro.modules.export.writers.ExportImageWriter
import app.anidro.modules.export.writers.ExportVideoWriter
import app.anidro.modules.persistence.settings.SettingsPersistence
import app.anidro.renderers.FixedFrameRateRenderer
import app.anidro.renderers.SequentialTimeNormalizer
import app.anidro.renderers.SingleFrameTimeNormalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class DrawingExporter(private val applicationContext: Context,
                      private val settingsPersistence: SettingsPersistence) : ExportFileWriter.ExportFileWriterCallbackListener {
    private val exportStateMutableLiveData: MutableLiveData<ExportState> = MutableLiveData<ExportState>().apply { value = ExportState.Initial }
    private val exportScope = CoroutineScope(Dispatchers.IO)
    private var exportJob: Job? = null
    private var fileWriter: ExportFileWriter? = null
    private var exportNotificationHelper: ExportNotificationPresenter? = null

    val exportStateLiveData: LiveData<ExportState> = exportStateMutableLiveData

    fun startExport(drawing: List<TimedSegment>,
                    @ColorInt backgroundColor: Int,
                    drawingWidth: Int,
                    drawingHeight: Int,
                    fileType: FileType) {
        if (exportJob?.isActive == true) {
            return
        }

        exportJob = exportScope.launch {
            doExport(drawing = drawing,
                    backgroundColor = backgroundColor,
                    drawingWidth = drawingWidth,
                    drawingHeight = drawingHeight,
                    fileType = fileType)
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        fileWriter?.cancel()
    }

    //region ExportFileWriter.ExportFileWriterCallbackListener

    @WorkerThread
    override fun onStart(total: Int) {
        exportJob?.isActive?.let {
            exportStateMutableLiveData.postValue(ExportState.InProgress(totalFramesCount = total, currentFrameNumber = 0))
        }
        exportNotificationHelper?.showExportNotificationBegin(total)
    }

    @WorkerThread
    override fun onProgress(current: Int, total: Int) {
        exportJob?.isActive?.let {
            exportStateMutableLiveData.postValue(ExportState.InProgress(totalFramesCount = total, currentFrameNumber = current))
        }
        exportNotificationHelper?.updateExportNotificationProgress(current, total)
    }

    @WorkerThread
    override fun onFinished(file: File, fileType: FileType, lastFrame: Bitmap) {
        if (exportJob?.isActive != true) {
            return
        }

        val fileContentUri = DrawingsFileHelper.getFileContentUri(applicationContext, file)
        val mimeType = getFileMimeType(fileType)
        exportNotificationHelper?.showExportNotificationEnd(fileContentUri, mimeType, lastFrame)
        exportStateMutableLiveData.postValue(ExportState.Finished(uri = fileContentUri, mimeType = mimeType))

        // If user setting is on and there is actually a granted permission for it,
        // we make a copy to the external user folder
        if (settingsPersistence.shouldCopyToExternal()) {
            if (ContextCompat.checkSelfPermission(applicationContext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                val externalFile = DrawingsFileHelper.copyToExternal(file, fileType)
                if (externalFile != null) {
                    DrawingsFileHelper.mediaStorageScan(applicationContext, externalFile)
                }
            } else {
                settingsPersistence.setCopyToExternal(false)
            }
        }
    }

    @WorkerThread
    override fun onFailed() {
        exportJob?.isActive?.let {
            exportNotificationHelper?.showExportFailed()
            exportStateMutableLiveData.postValue(ExportState.Failed)
        }
    }

    @WorkerThread
    override fun onCancelled() {
        exportNotificationHelper?.hideNotification()
        exportStateMutableLiveData.postValue(ExportState.Cancelled)
    }

    //endregion ExportFileWriter.ExportFileWriterCallbackListener

    @WorkerThread
    private fun doExport(drawing: List<TimedSegment>,
                         @ColorInt backgroundColor: Int,
                         drawingWidth: Int,
                         drawingHeight: Int,
                         fileType: FileType) {
        // Show notifications only if the user has not deactivated them
        if (settingsPersistence.shouldShowExportNotification()) {
            exportNotificationHelper = ExportNotificationPresenter(applicationContext, fileType)
        }

        // Init renderer
        val renderer = buildRenderer(drawing, backgroundColor, drawingWidth, drawingHeight, fileType)

        // Init file writer
        when (fileType) {
            FileType.GIF -> fileWriter = ExportGifWriter(applicationContext, renderer, this@DrawingExporter)
            FileType.IMAGE -> fileWriter = ExportImageWriter(applicationContext, renderer, this@DrawingExporter)
            FileType.VIDEO -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    fileWriter = ExportVideoWriter(applicationContext, renderer, this@DrawingExporter)
                } else {
                    // No video support pre 4.3
                    renderer.destroy()
                }
            }
            FileType.NONE -> { // nothing to do
            }
        }

        // Export
        fileWriter?.writeFile()
    }


    private fun buildRenderer(drawing: List<TimedSegment>,
                              @ColorInt backgroundColor: Int,
                              drawingWidth: Int,
                              drawingHeight: Int,
                              fileType: FileType): FixedFrameRateRenderer {
        when (fileType) {
            FileType.IMAGE -> {
                return FixedFrameRateRenderer(drawing, backgroundColor, drawingWidth, drawingHeight,
                        SingleFrameTimeNormalizer(), 100, Bitmap.Config.ARGB_8888)
            }
            FileType.VIDEO -> {
                // Reduce size of the frame for videos because of crashes in the codec and size concerns
                val size = limitExportResolution(drawingWidth, drawingHeight, 1280)

                val renderer = FixedFrameRateRenderer(drawing, backgroundColor, size.x, size.y,
                        SequentialTimeNormalizer(), FixedFrameRateRenderer.VIDEO_FRAME_LENGTH, Bitmap.Config.ARGB_8888)
                renderer.addFinalFrameExtraDelay(FixedFrameRateRenderer.ANIMATION_FINAL_FRAME_EXTRA_LENGTH)
                return renderer
            }
            FileType.GIF -> {
                // Reduce size of the frame for GIFs because of really bad performance for higher resolution
                val size = limitExportResolution(drawingWidth, drawingHeight, 480)

                return FixedFrameRateRenderer(drawing, backgroundColor, size.x, size.y,
                        SequentialTimeNormalizer(), FixedFrameRateRenderer.GIF_FRAME_LENGTH, Bitmap.Config.RGB_565)
            }
            else -> {
                val size = limitExportResolution(drawingWidth, drawingHeight, 480)
                return FixedFrameRateRenderer(drawing, backgroundColor, size.x, size.y, SequentialTimeNormalizer(), FixedFrameRateRenderer.GIF_FRAME_LENGTH, Bitmap.Config.RGB_565)
            }
        }
    }

    private fun limitExportResolution(width: Int, height: Int, maxBiggerDimension: Int): Point {
        val coef = width.toDouble() / height.toDouble()
        if (width > height) {
            if (width > maxBiggerDimension) {
                return Point(maxBiggerDimension, (maxBiggerDimension.toDouble() / coef).toInt())
            }
        } else {
            if (height > maxBiggerDimension) {
                return Point((maxBiggerDimension.toDouble() * coef).toInt(), maxBiggerDimension)
            }
        }

        // Nothing to reduce
        return Point(width, height)
    }

    private fun getFileMimeType(fileType: FileType) =
            when (fileType) {
                FileType.GIF -> "image/gif"
                FileType.VIDEO -> "video/mp4"
                FileType.IMAGE -> "image/jpeg"
                else -> "image/jpeg"
            }
}