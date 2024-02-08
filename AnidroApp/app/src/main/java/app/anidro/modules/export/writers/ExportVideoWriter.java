package app.anidro.modules.export.writers;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.os.Build;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import java.io.File;

import app.anidro.models.FileType;
import app.anidro.modules.export.video.VideoEncoderCore;
import app.anidro.renderers.FixedFrameRateRenderer;

/**
 * An exported which encodes each frame as a frame of a video.
 * It adds some extra delay for the last frame. This writer is supported
 * only for Android 4.3 and newer, since the used video encoding api is not
 * available for older devices.
 * <p/>
 * Created by luboganev on 27/09/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ExportVideoWriter extends ExportFileWriter {

    private static final int BIT_RATE = 2000000;

    private VideoEncoderCore encoder;
    private Surface encoderSurface;
    private final int width;
    private final int height;
    private int left = 0;
    private int top = 0;

    private final static float SUPPORTED_RATIO = 4.0F / 3.0F;
    private final static float SUPPORTED_RATIO_SQUARE = 16.0F / 9.0F;

    public ExportVideoWriter(Context applicationContext, FixedFrameRateRenderer frameRenderer, ExportFileWriterCallbackListener listener) {
        super(applicationContext, frameRenderer, listener);
        width = frameRenderer.getFrameWidth();
        height = frameRenderer.getFrameHeight();
    }

    @Override
    protected void startWrite(File file) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lollipopSetupVideoEncoder(file);
        } else {
            encoder = new VideoEncoderCore(width, height, BIT_RATE, file);
        }
        encoderSurface = encoder.getInputSurface();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void lollipopSetupVideoEncoder(File file) throws Exception {
        try {
            // try the current dimensions
            encoder = new VideoEncoderCore(width, height, BIT_RATE, file);
        } catch (MediaCodec.CodecException e) {
            Size fallbackSize = calculateNewFrameSize(width, height, 4.0f / 3.0f);
            // try 4:3 or 3:4 aspect ratio (with black lines)
            try {
                encoder = new VideoEncoderCore(fallbackSize.getWidth(), fallbackSize.getHeight(), BIT_RATE, file);
            } catch (MediaCodec.CodecException ex) {
                fallbackSize = calculateNewFrameSize(width, height, 16.0f / 9.0f);
                // try 16:9 or 9:16 aspect ratio (with black lines)
                encoder = new VideoEncoderCore(fallbackSize.getWidth(), fallbackSize.getHeight(), BIT_RATE, file);
            }

            left = (fallbackSize.getWidth() - width) / 2;
            top = (fallbackSize.getHeight() - height) / 2;
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size calculateNewFrameSize(int width, int height, float supportedRatio) {
        final float supportedReverseRatio = 1.0f / supportedRatio;
        int frameWidth;
        int frameHeight;

        if (width >= height) {
            if ((((float)width) / ((float)height)) < supportedRatio) {
                frameHeight = height;
                frameWidth = (int) (((float)height) * supportedRatio);
            } else {
                frameWidth = width;
                frameHeight = (int) (frameWidth * supportedReverseRatio);
            }
        } else {
            if ((((float)height) / ((float)width)) < supportedRatio) {
                frameWidth = width;
                frameHeight = (int) (frameWidth * supportedRatio);
            } else {
                frameHeight = height;
                frameWidth = (int) (frameHeight * supportedReverseRatio);
            }
        }

        return new Size(frameWidth, frameHeight);
    }

    @Override
    protected void endWrite() throws Exception {
        if (encoder == null) {
            return;
        }
        encoder.release();
    }

    @Override
    protected void writeFrame(Bitmap currentFrame, boolean isLastFrame) throws Exception {
        writeFrameToSurface(currentFrame, isLastFrame);
    }

    @Override
    protected FileType getFileType() {
        return FileType.VIDEO;
    }

    private void writeFrameToSurface(Bitmap currentFrame, boolean isLastFrame) {
        if (encoder == null || encoderSurface == null) {
            return;
        }

        Canvas canvas = encoderSurface.lockCanvas(null);

        canvas.drawBitmap(currentFrame, left, top, null);

        encoderSurface.unlockCanvasAndPost(canvas);
        encoder.drainEncoder(isLastFrame);
    }
}
