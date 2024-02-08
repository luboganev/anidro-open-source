package app.anidro.modules.export.writers;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.File;

import app.anidro.models.FileType;
import app.anidro.modules.export.files.DrawingsFileHelper;
import app.anidro.renderers.FixedFrameRateRenderer;
import timber.log.Timber;

/**
 * A base drawing exporter. It manages the creation of files, rendering of drawing frames and calls
 * its children to perform the specific frame encoding depending on the exported file type.
 * It also supports cancelling of running export operation.
 * <p/>
 * Created by luboganev on 27/09/15.
 */
public abstract class ExportFileWriter {
    private final FixedFrameRateRenderer frameRenderer;
    private final ExportFileWriterCallbackListener listener;
    private final Context applicationContext;
    private boolean isCancelled;

    /**
     * A callback to be implemented by the caller of the exporting
     */
    public interface ExportFileWriterCallbackListener {
        void onStart(int total);
        void onProgress(int current, int total);
        void onFinished(File file, FileType fileType, Bitmap lastFrame);
        void onFailed();
        void onCancelled();
    }

    public ExportFileWriter(@NonNull Context applicationContext, @NonNull FixedFrameRateRenderer frameRenderer, @NonNull ExportFileWriterCallbackListener listener) {
        this.frameRenderer = frameRenderer;
        this.listener = listener;
        this.isCancelled = false;
        this.applicationContext = applicationContext;
    }

    /**
     * Cancels running export
     */
    public void cancel() {
        isCancelled = true;
    }

    /**
     * Called initially when and export operation starts and the file is ready for writing.
     * @throws Exception
     *      The implementation might fail for some reason
     */
    protected abstract void startWrite(File file) throws Exception;

    /**
     * Called when all drawing frames have been already written to the exported file.
     * @throws Exception
     *      The implementation might fail for some reason
     */
    protected abstract void endWrite() throws Exception;

    /**
     * Called on every drawing frame.
     * @throws Exception
     *      The implementation might fail for some reason
     */
    protected abstract void writeFrame(Bitmap currentFrame, boolean isLastFrame) throws Exception;

    /**
     * Returns the exported file type
     */
    protected abstract FileType getFileType();

    /**
     * Starts the exporting of the drawing.
     */
    public void writeFile() {
        frameRenderer.resetRenderer();
        isCancelled = false;

        DrawingsFileHelper.deleteDrawingsOlderThanOneDay(applicationContext);
        File file = DrawingsFileHelper.getFreshDrawingFile(applicationContext, getFileType());

        if (file == null) {
            listener.onFailed();
            return;
        }

        boolean hasFailed = false;

        listener.onStart(frameRenderer.getFramesCount());

        try {
            try {
                startWrite(file);
            } catch (Exception e) {
                hasFailed = true;
                Timber.e(e, "Crash when starting to write to file");
            }

            while (frameRenderer.hasNextFrame() && !isCancelled && !hasFailed) {
                frameRenderer.renderNextFrame();

                try {
                    writeFrame(frameRenderer.getCurrentFrame(), !frameRenderer.hasNextFrame());
                } catch (Exception e) {
                    hasFailed = true;
                    Timber.e(e, "Crash white writing to file");
                }

                listener.onProgress(frameRenderer.getCurrentFrameIndex(), frameRenderer.getFramesCount());
            }

        } catch (Exception e) {
            hasFailed = true;
            Timber.e(e, "Renderer exception while writing to file");
        } finally {
            try {
                endWrite();
            } catch (Exception e) {
                hasFailed = true;
                Timber.e(e, "Crash when finishing to write to file");
            }
        }

        if (hasFailed) {
            listener.onFailed();
            cleanupOnCancelOrFail(file);
        } else if (isCancelled) {
            listener.onCancelled();
            cleanupOnCancelOrFail(file);
        } else {
            listener.onFinished(file, getFileType(), frameRenderer.getCurrentFrame());
        }
    }

    /**
     * Cleans up unnecessary created file if export fails or is canceled
     */
    private void cleanupOnCancelOrFail(File exportedFile) {
        if (exportedFile != null && exportedFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            exportedFile.delete();
        }
    }
}