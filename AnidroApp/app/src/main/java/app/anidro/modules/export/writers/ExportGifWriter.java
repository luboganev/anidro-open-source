package app.anidro.modules.export.writers;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

import app.anidro.models.FileType;
import app.anidro.modules.export.gif.AnimatedGifEncoder;
import app.anidro.renderers.FixedFrameRateRenderer;

/**
 * An exported which encodes each frame as a frame of a GIF image.
 * It adds some extra delay for the last frame.
 * <p/>
 * Created by luboganev on 27/09/15.
 */
public class ExportGifWriter extends ExportFileWriter {
    private final AnimatedGifEncoder encoder;
    private FileOutputStream fos;

    public ExportGifWriter(Context applicationContext, FixedFrameRateRenderer frameRenderer, ExportFileWriterCallbackListener listener) {
        super(applicationContext, frameRenderer, listener);
        encoder = new AnimatedGifEncoder();
        encoder.setDelay((int) FixedFrameRateRenderer.GIF_FRAME_LENGTH);
        encoder.setRepeat(0);
    }

    @Override
    protected void startWrite(File file) throws Exception {
        fos = new FileOutputStream(file);
        encoder.start(fos);
    }

    @Override
    protected void endWrite() throws Exception {
        if (encoder == null) {
            return;
        }
        encoder.finish();
        if (fos == null) {
            return;
        }
        fos.close();
    }

    @Override
    protected void writeFrame(Bitmap currentFrame, boolean isLastFrame) throws Exception {
        if (encoder == null) {
            return;
        }
        if (isLastFrame) {
            encoder.setDelay((int)(FixedFrameRateRenderer.GIF_FRAME_LENGTH +
                        FixedFrameRateRenderer.ANIMATION_FINAL_FRAME_EXTRA_LENGTH));
        }
        encoder.addFrame(currentFrame);
    }

    @Override
    protected FileType getFileType() {
        return FileType.GIF;
    }
}
