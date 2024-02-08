package app.anidro.modules.main.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.view.TextureView;

import androidx.annotation.ColorInt;

import java.util.List;

import app.anidro.models.TimedSegment;
import app.anidro.renderers.FixedFrameRateRenderer;
import app.anidro.renderers.SequentialTimeNormalizer;

/**
 * A custom {@link TextureView} which shows a live preview of the animated drawing.
 * <p/>
 * Created by luboganev on 02/08/15.
 */
public class DrawingAnimationView extends TextureView implements TextureView.SurfaceTextureListener {
    private RenderThread renderThread;

    private interface RenderThread {
        void start();

        void stopRendering();
    }

    private boolean isSurfaceAvailable = false;
    private boolean isAnimating = false;
    private List<TimedSegment> segments;
    private int backgroundColor;

    public DrawingAnimationView(Context context) {
        super(context);
        init();
    }

    public DrawingAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawingAnimationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOpaque(false);
        setSurfaceTextureListener(this);
    }

    private int retryStartAnimation;

    /**
     * Starts the animated preview of a particular drawing
     */
    public void startAnimation(List<TimedSegment> segments, @ColorInt int backgroundColor) {
        if (!isAnimating) {
            this.segments = segments;
            this.backgroundColor = backgroundColor;
            isAnimating = true;
            if (isSurfaceAvailable) {
                startRenderThread();
            } else {
                retryStartAnimation = 0;
                postDelayed(delayedStartAnimationRunnable, ANIMATION_START_RETRY_DELAY);
            }
        }
    }

    private static final int ANIMATION_START_RETRY_DELAY = 500;
    private static final int ANIMATION_START_RETRY_MAX = 5;

    /**
     * Sometimes the surface needs some time to get ready
     */
    private Runnable delayedStartAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            DrawingAnimationView.this.retryStartAnimation++;
            if (DrawingAnimationView.this.isSurfaceAvailable) {
                DrawingAnimationView.this.startRenderThread();
            } else if (DrawingAnimationView.this.retryStartAnimation < DrawingAnimationView.ANIMATION_START_RETRY_MAX) {
                DrawingAnimationView.this.postDelayed(this, ANIMATION_START_RETRY_DELAY);
            }
        }
    };

    /**
     * Stops the animated preview of the drawing
     */
    public void stopAnimation() {
        if (isAnimating) {
            removeCallbacks(delayedStartAnimationRunnable);
            stopRenderThread();
            isAnimating = false;
        }
    }

    private void startRenderThread() {
        if (renderThread == null) {
            renderThread = new AnimatedPreviewThread(this, segments, backgroundColor, FixedFrameRateRenderer.PREVIEW_FRAME_LENGTH, true);
            renderThread.start();
        }
    }

    private void stopRenderThread() {
        if (renderThread != null) {
            renderThread.stopRendering();
            renderThread = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        isSurfaceAvailable = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // do nothing
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopRenderThread();
        isSurfaceAvailable = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // do nothing
    }

    /**
     * This class encapsulates the rendering of the animated preview of the drawing.
     * It loops through all the drawing frames over and over again.
     */
    private static class AnimatedPreviewThread extends Thread implements RenderThread {
        private final TextureView surface;
        private volatile boolean running = true;
        private final FixedFrameRateRenderer fixedFrameRateRenderer;
        private final boolean loop;
        private final long frameLength;

        public AnimatedPreviewThread(TextureView surface, List<TimedSegment> segments, @ColorInt int backgroundColor, long frameLength, boolean loop) {
            this.surface = surface;
            this.frameLength = frameLength;
            this.loop = loop;
            this.fixedFrameRateRenderer = new FixedFrameRateRenderer(segments, backgroundColor,
                    this.surface.getWidth(), this.surface.getHeight(),
                    new SequentialTimeNormalizer(), this.frameLength, Bitmap.Config.ARGB_8888);
            this.fixedFrameRateRenderer.addFinalFrameExtraDelay(FixedFrameRateRenderer.ANIMATION_FINAL_FRAME_EXTRA_LENGTH);
        }

        @Override
        public void run() {
            long sleepTime = 0;
            long currentFrameDrawTime;

            while (running && !Thread.interrupted()) {
                currentFrameDrawTime = System.currentTimeMillis();

                if (surface.isAvailable()) {
                    final Canvas canvas = surface.lockCanvas(null);
                    try {
                        // Clear canvas
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                        // Check if we have to reset the renderer
                        if (!fixedFrameRateRenderer.hasNextFrame()) {
                            fixedFrameRateRenderer.resetRenderer();
                        }

                        // Render the frame and draw it on the canvas
                        fixedFrameRateRenderer.renderNextFrame();
                        canvas.drawBitmap(fixedFrameRateRenderer.getCurrentFrame(), 0, 0, null);

                        currentFrameDrawTime = System.currentTimeMillis() - currentFrameDrawTime;

                        // Decide on sleep time
                        if (!fixedFrameRateRenderer.hasNextFrame() && !loop) {
                            break;
                        }
                        sleepTime = frameLength - currentFrameDrawTime;
                    } finally {
                        surface.unlockCanvasAndPost(canvas);
                    }
                } else {
                    sleepTime = 100;
                }


                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // Interrupted
                    }
                }
            }
            fixedFrameRateRenderer.destroy();
        }

        public void stopRendering() {
            running = false;
            interrupt();
        }
    }
}
