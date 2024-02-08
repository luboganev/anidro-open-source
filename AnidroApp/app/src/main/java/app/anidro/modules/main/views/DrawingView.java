package app.anidro.modules.main.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import app.anidro.common.DrawingColorsPalette;
import app.anidro.common.DrawingPaintBuilder;
import app.anidro.modules.main.DrawingDelegate;

/**
 * A custom drawing view which allows the user to perform single touch finger draw.
 */
public class DrawingView extends View {

    private PointF lastTouchPoint;
    @SuppressWarnings("NullableProblems")
    private @NonNull Paint drawPaint;
    @SuppressWarnings("NullableProblems")
    private @NonNull Paint canvasPaint;
    @SuppressWarnings("NullableProblems")
    private @NonNull Canvas drawCanvas;
    @SuppressWarnings("NullableProblems")
    private @NonNull Bitmap canvasBitmap;
    @SuppressWarnings("NullableProblems")
    private @NonNull DrawingDelegate drawingDelegate;

    // If canvas should be cleared in onDraw
    private boolean clear = false;

    // If cached bitmap should be redrawn in onDraw
    private boolean redrawCanvasBitmap = false;



    // View methods and overrides

    public DrawingView(Context context) {
        super(context);
        initView();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        if (isInEditMode()) {
            return;
        }

        drawPaint = DrawingPaintBuilder.getPaint(new DrawingColorsPalette(getResources()).getDefaultDrawingColor(), getResources(), DrawingPaintBuilder.DEFAULT_STROKE_WIDTH_DP);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        // Check if we have missed any move events
        if (event.getHistorySize() > 0 && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                processTouchEvent(event.getHistoricalX(i), event.getHistoricalY(i), MotionEvent.ACTION_MOVE);
            }
        }

        // Process the current event
        processTouchEvent(event.getX(), event.getY(), event.getActionMasked());

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (isInEditMode()) {
            return;
        }

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawingDelegate.redrawCurrentDrawing(drawCanvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }

        if (clear) {
            drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawingDelegate.redrawBackground(drawCanvas);
            clear = false;
        }

        if (redrawCanvasBitmap) {
            drawingDelegate.redrawCurrentDrawing(drawCanvas);
            redrawCanvasBitmap = false;
        }

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
    }

    /**
     * Processes a touch event if the delegate recognizes it as a valid drawing event
     */
    private void processTouchEvent(float touchX, float touchY, int touchActionMasked) {
        if (drawingDelegate.onTouchEvent(touchX, touchY, touchActionMasked)) {

            if (lastTouchPoint == null) {
                // in case lastTouchPoint is null and
                // action down is skipped for some reason.
                lastTouchPoint = new PointF(touchX, touchY);
            }

            switch (touchActionMasked) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchPoint = new PointF(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (lastTouchPoint.equals(touchX, touchY)) {
                        drawCanvas.drawPoint(lastTouchPoint.x, lastTouchPoint.y, drawPaint);
                    } else {
                        drawCanvas.drawLine(lastTouchPoint.x, lastTouchPoint.y, touchX, touchY, drawPaint);
                        lastTouchPoint = new PointF(touchX, touchY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (lastTouchPoint.equals(touchX, touchY)) {
                        drawCanvas.drawPoint(lastTouchPoint.x, lastTouchPoint.y, drawPaint);
                    } else {
                        drawCanvas.drawLine(lastTouchPoint.x, lastTouchPoint.y, touchX, touchY, drawPaint);
                    }
                    lastTouchPoint = null;
                    break;
            }

            invalidate();
        }
    }



    // Public API

    /**
     *  Sets the {@link DrawingDelegate} needed by this view in order to be able to render
     *  the current drawing
     */
    public void setDrawingDelegate(@NonNull DrawingDelegate drawingDelegate) {
        this.drawingDelegate = drawingDelegate;
        redraw();
    }

    /**
     *  This callback should be called each time the brush of the current drawing changes its color
     *  or its stroke width
     */
    public void onBrushChanged() {
        drawPaint.setColor(drawingDelegate.getBrushColor());
        drawPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                drawingDelegate.getBrushStrokeWidth(), getResources().getDisplayMetrics()));
    }

    /**
     *  Clears the canvas from any drawing and redraws the current drawing background color
     */
    public void clearCanvas() {
        clear = true;
        invalidate();
    }

    /**
     *  Forces this view to redraw the whole current drawing
     */
    public void redraw() {
        redrawCanvasBitmap = true;
        invalidate();
    }
}
