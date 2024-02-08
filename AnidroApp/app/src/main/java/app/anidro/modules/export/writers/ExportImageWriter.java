package app.anidro.modules.export.writers;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

import app.anidro.models.FileType;
import app.anidro.renderers.FixedFrameRateRenderer;

/**
 * A simple writer which encodes the frame as a simple JPG image
 * <p/>
 * Created by luboganev on 27/09/15.
 */
public class ExportImageWriter extends ExportFileWriter {
    private FileOutputStream fos;

    public ExportImageWriter(Context applicationContext, FixedFrameRateRenderer frameRenderer, ExportFileWriterCallbackListener listener) {
        super(applicationContext, frameRenderer, listener);
    }

    @Override
    protected void startWrite(File file) throws Exception {
        fos = new FileOutputStream(file);
    }

    @Override
    protected void endWrite() throws Exception {
        if (fos == null) {
            return;
        }
        fos.close();
    }

    @Override
    protected void writeFrame(Bitmap currentFrame, boolean isLastFrame) throws Exception {
        if (fos == null) {
            return;
        }
        // Writing the bitmap to the output file
        currentFrame.compress(Bitmap.CompressFormat.JPEG, 80, fos);
    }

    @Override
    protected FileType getFileType() {
        return FileType.IMAGE;
    }
}
