package app.anidro.modules.main;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.text.format.DateUtils;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.anidro.common.DisplayMetricsConverter;
import app.anidro.common.DrawingColorsPalette;
import app.anidro.common.DrawingPaintBuilder;
import app.anidro.common.ScreenOrientationSensor;
import app.anidro.models.TimedPoint;
import app.anidro.models.TimedSegment;

/**
 * This class contains everything related to the state and configuration of the current drawing.
 * <p/>
 * Created by luboganev on 13/02/16.
 */
public class TimedDrawingManager implements DrawingDelegate {

    /* Declarations */
    public interface TimedDrawingCallbackListener {
        void onDrawingStarted();

        void onDrawingStopped();

        void onDrawingTimeProgressChanged(int drawingTimeProgress);

        void onDrawingLimitExceeded();
    }

    /**
     * A helper class which also contains the touch event action type
     */
    private static class TouchTimedPoint {
        final TimedPoint timedPoint;
        final int touchEventAction;
        final ScreenOrientationSensor.ScreenOrientation screenOrientation;
        final float canvasWidth;
        final float canvasHeight;

        public TouchTimedPoint(TimedPoint timedPoint, int touchEventAction,
                               ScreenOrientationSensor.ScreenOrientation screenOrientation,
                               float canvasWidth, float canvasHeight) {
            this.timedPoint = timedPoint;
            this.touchEventAction = touchEventAction;
            this.screenOrientation = screenOrientation;
            this.canvasWidth = canvasWidth;
            this.canvasHeight = canvasHeight;
        }

        public TouchTimedPoint(float x, float y, int touchEventAction, long timestamp,
                               ScreenOrientationSensor.ScreenOrientation screenOrientation,
                               float canvasWidth, float canvasHeight) {
            this(
                    new TimedPoint(x, y, timestamp),
                    touchEventAction,
                    screenOrientation,
                    canvasWidth,
                    canvasHeight);
        }
    }

    /* Constants */
    private static final long MAX_DRAWING_TIME = 30 * DateUtils.SECOND_IN_MILLIS;

    /* Dependencies */
    private final DrawingColorsPalette drawingColorsPalette;
    private final DisplayMetricsConverter displayMetricsConverter;


    /* State */
    private List<TouchTimedPoint> touchTimedPoints;
    private List<Integer> segmentColorIndexes;
    private List<Integer> segmentColorLightnessIndexes;
    private List<Integer> segmentStrokeDPWidths;
    private long currentDrawingTime = 0;
    private TimedDrawingCallbackListener listener;

    // If the callback for exceeded drawing time should be called
    private boolean shouldNotifyLimitExceeded;

    private int currentColorIndex;
    private int currentColorLightnessIndex;
    private int currentStrokeDPWidth;

    private int backgroundColorIndex;
    private int backgroundColorLightnessIndex;

    private int canvasWidth;
    private int canvasHeight;
    @Nullable
    private ScreenOrientationSensor.ScreenOrientation screenOrientation;

    public TimedDrawingManager(DrawingColorsPalette drawingColorsPalette, DisplayMetricsConverter displayMetricsConverter) {
        this.drawingColorsPalette = drawingColorsPalette;
        this.displayMetricsConverter = displayMetricsConverter;

        this.currentColorIndex = DrawingColorsPalette.DEFAULT_BRUSH_COLOR_INDEX;
        this.currentColorLightnessIndex = DrawingColorsPalette.DEFAULT_BRUSH_COLOR_LIGHTNESS_INDEX;
        this.backgroundColorIndex = DrawingColorsPalette.DEFAULT_BACKGROUND_COLOR_INDEX;
        this.backgroundColorLightnessIndex = DrawingColorsPalette.DEFAULT_BACKGROUND_COLOR_LIGHTNESS_INDEX;
        this.currentStrokeDPWidth = DrawingPaintBuilder.DEFAULT_STROKE_WIDTH_DP;
        this.shouldNotifyLimitExceeded = true;
        this.currentDrawingTime = 0;
        this.touchTimedPoints = new ArrayList<>();
        this.segmentColorIndexes = new ArrayList<>();
        this.segmentColorLightnessIndexes = new ArrayList<>();
        this.segmentStrokeDPWidths = new ArrayList<>();
        this.canvasWidth = 0;
        this.canvasHeight = 0;
        // no value first
        this.screenOrientation = null;
    }



    /* Public API */

    public void setColor(int colorIndex, int colorLightnessIndex) {
        currentColorIndex = colorIndex;
        currentColorLightnessIndex = colorLightnessIndex;
    }

    public void setBackgroundColor(int colorIndex, int colorLightnessIndex) {
        backgroundColorIndex = colorIndex;
        backgroundColorLightnessIndex = colorLightnessIndex;
    }

    public void setStrokeWidth(int strokeWidth) {
        currentStrokeDPWidth = strokeWidth;
    }

    public int getCurrentColorIndex() {
        return currentColorIndex;
    }

    public int getCurrentColorLightnessIndex() {
        return currentColorLightnessIndex;
    }

    public int getCurrentStrokeDPWidth() {
        return currentStrokeDPWidth;
    }

    public int getBackgroundColorIndex() {
        return backgroundColorIndex;
    }

    public int getBackgroundColorLightnessIndex() {
        return backgroundColorLightnessIndex;
    }

    public @ColorInt
    int getBackgroundColor() {
        return drawingColorsPalette.getColorWithLightness(backgroundColorIndex, backgroundColorLightnessIndex);
    }

    public void setCanvasSize(int newCanvasWidth, int newCanvasHeight, ScreenOrientationSensor.ScreenOrientation newScreenOrientation) {
        if (newCanvasWidth <= 0 || newCanvasHeight <= 0) {
            return;
        }

        canvasWidth = newCanvasWidth;
        canvasHeight = newCanvasHeight;
        screenOrientation = newScreenOrientation;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    @Nullable
    public ScreenOrientationSensor.ScreenOrientation getScreenOrientation() {
        return screenOrientation;
    }

    @NonNull
    public List<TimedSegment> getTimedSegments() {
        List<TimedSegment> segments = new ArrayList<>();

        List<TimedPoint> points = new ArrayList<>();

        int segmentIndex = -1;
        for (int i = 0; i < touchTimedPoints.size(); i++) {
            TouchTimedPoint p = adjustForCurrentCanvas(touchTimedPoints.get(i));

            if (p.touchEventAction == MotionEvent.ACTION_DOWN && !points.isEmpty()) {
                segmentIndex++;
                segments.add(new TimedSegment(points, canvasWidth, canvasHeight, getSegmentPaint(segmentIndex)));
                points.clear();
            }

            points.add(new TimedPoint(p.timedPoint.getX(), p.timedPoint.getY(), p.timedPoint.getTimestamp()));
        }

        if (!points.isEmpty()) {
            segmentIndex++;
            segments.add(new TimedSegment(points, canvasWidth, canvasHeight, getSegmentPaint(segmentIndex)));
        }

        return segments;
    }

    public void clear() {
        touchTimedPoints.clear();
        segmentColorIndexes.clear();
        segmentColorLightnessIndexes.clear();
        segmentStrokeDPWidths.clear();
        currentDrawingTime = 0;
        notifyDrawingProgressChanged();
    }

    public boolean removeLastDrawingSegment() {
        boolean shouldRedraw = false;
        if (!touchTimedPoints.isEmpty()) {
            for (int i = touchTimedPoints.size() - 1; i >= 0; i--) {
                if (touchTimedPoints.get(i).touchEventAction == MotionEvent.ACTION_DOWN) {
                    if (i == 0) {
                        touchTimedPoints.clear();
                    } else {
                        touchTimedPoints = touchTimedPoints.subList(0, i);
                    }
                    shouldRedraw = true;
                    break;
                }
            }
            segmentColorIndexes.remove(segmentColorIndexes.size() - 1);
            segmentColorLightnessIndexes.remove(segmentColorLightnessIndexes.size() - 1);
            segmentStrokeDPWidths.remove(segmentStrokeDPWidths.size() - 1);
            recalculateDrawingTime();
        }
        return shouldRedraw;
    }

    public boolean isDrawingEmpty() {
        return touchTimedPoints.isEmpty();
    }

    public void setListener(TimedDrawingCallbackListener listener) {
        this.listener = listener;
    }

    public int getDrawingProgress() {
        if (currentDrawingTime > MAX_DRAWING_TIME) {
            return 100;
        }

        return (int) Math.floor(
                ((double) currentDrawingTime) /
                        ((double) MAX_DRAWING_TIME)
                        * 100.0d);
    }



    /* DrawingDelegate implementation */

    @Override
    public boolean onTouchEvent(float touchX, float touchY, int touchActionMasked) {
        // Supported touch events
        if (touchActionMasked != MotionEvent.ACTION_DOWN
                && touchActionMasked != MotionEvent.ACTION_UP
                && touchActionMasked != MotionEvent.ACTION_MOVE) {
            return false;
        }

        if (touchActionMasked == MotionEvent.ACTION_DOWN) {
            shouldNotifyLimitExceeded = true;
        }

        if (currentDrawingTime > MAX_DRAWING_TIME) {
            if (shouldNotifyLimitExceeded) {
                shouldNotifyLimitExceeded = false;
                if (listener != null) {
                    listener.onDrawingLimitExceeded();
                }
            }
            return false;
        }

        TouchTimedPoint lastTimedPoint = !touchTimedPoints.isEmpty() ? touchTimedPoints.get(touchTimedPoints.size() - 1) : null;

        // Framework weird behavior, sending up or move action before down. We make sure this is not happening
        if (lastTimedPoint == null && touchActionMasked != MotionEvent.ACTION_DOWN) {
            touchActionMasked = MotionEvent.ACTION_DOWN;
        }

        TouchTimedPoint currentTimedPoint = new TouchTimedPoint(
                touchX, touchY, touchActionMasked, System.currentTimeMillis(), screenOrientation, canvasWidth, canvasHeight);
        touchTimedPoints.add(currentTimedPoint);

        switch (touchActionMasked) {
            case MotionEvent.ACTION_DOWN:
                segmentColorIndexes.add(currentColorIndex);
                segmentColorLightnessIndexes.add(currentColorLightnessIndex);
                segmentStrokeDPWidths.add(currentStrokeDPWidth);
                if (lastTimedPoint != null) {
                    currentDrawingTime += 100;
                    notifyDrawingProgressChanged();
                }
                if (listener != null) {
                    listener.onDrawingStarted();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentDrawingTime += currentTimedPoint.timedPoint.timeTo(lastTimedPoint.timedPoint);
                notifyDrawingProgressChanged();
                break;
            case MotionEvent.ACTION_UP:
                currentDrawingTime += currentTimedPoint.timedPoint.timeTo(lastTimedPoint.timedPoint);
                if (listener != null) {
                    listener.onDrawingStopped();
                }
                notifyDrawingProgressChanged();
                break;
        }

        return true;
    }

    public void redrawCurrentDrawing(Canvas drawCanvas) {
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawCanvas.drawColor(
                drawingColorsPalette.getColorWithLightness(backgroundColorIndex, backgroundColorLightnessIndex));
        if (!touchTimedPoints.isEmpty()) {

            PointF previousDrawingPoint = null;
            int segmentIndex = 0;
            for (int i = 0; i < touchTimedPoints.size(); i++) {
                TouchTimedPoint p = adjustForCurrentCanvas(touchTimedPoints.get(i));

                if (previousDrawingPoint == null) {
                    // in case the action down is skipped for some reason
                    previousDrawingPoint = new PointF(p.timedPoint.getX(), p.timedPoint.getY());
                }

                if (p.touchEventAction == MotionEvent.ACTION_DOWN) {
                    previousDrawingPoint = new PointF(p.timedPoint.getX(), p.timedPoint.getY());
                    continue;
                }

                if (p.touchEventAction == MotionEvent.ACTION_MOVE) {
                    if (previousDrawingPoint.equals(p.timedPoint.getX(), p.timedPoint.getY())) {
                        drawCanvas.drawPoint(p.timedPoint.getX(), p.timedPoint.getY(), getSegmentPaint(segmentIndex));
                    } else {
                        drawCanvas.drawLine(previousDrawingPoint.x, previousDrawingPoint.y, p.timedPoint.getX(), p.timedPoint.getY(), getSegmentPaint(segmentIndex));
                    }
                    previousDrawingPoint = new PointF(p.timedPoint.getX(), p.timedPoint.getY());
                    continue;
                }

                if (p.touchEventAction == MotionEvent.ACTION_UP) {
                    if (previousDrawingPoint.equals(p.timedPoint.getX(), p.timedPoint.getY())) {
                        drawCanvas.drawPoint(p.timedPoint.getX(), p.timedPoint.getY(), getSegmentPaint(segmentIndex));
                    } else {
                        drawCanvas.drawLine(previousDrawingPoint.x, previousDrawingPoint.y, p.timedPoint.getX(), p.timedPoint.getY(), getSegmentPaint(segmentIndex));
                    }
                }

                previousDrawingPoint = null;
                segmentIndex++;
            }
        }
    }

    @Override
    public void redrawBackground(Canvas drawCanvas) {
        drawCanvas.drawColor(
                drawingColorsPalette.getColorWithLightness(
                        backgroundColorIndex,
                        backgroundColorLightnessIndex)
        );
    }

    @Override
    public int getBrushColor() {
        return drawingColorsPalette.getColorWithLightness(
                currentColorIndex,
                currentColorLightnessIndex);
    }

    @Override
    public int getBrushStrokeWidth() {
        return currentStrokeDPWidth;
    }



    /* Helper methods */

    private Paint getSegmentPaint(int segmentIndex) {
        return DrawingPaintBuilder.getPaint(

                drawingColorsPalette.getColorWithLightness(
                        segmentColorIndexes.get(segmentIndex),
                        segmentColorLightnessIndexes.get(segmentIndex)),

                displayMetricsConverter.pixelsFromDp(segmentStrokeDPWidths.get(segmentIndex))
        );
    }

    private TouchTimedPoint adjustForCurrentCanvas(TouchTimedPoint point) {
        float x = point.timedPoint.getX();
        float y = point.timedPoint.getY();

        ScreenOrientationSensor.ScreenOrientation rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_0;

        if (screenOrientation != null) {
            switch (point.screenOrientation) {
                case ORIENTATION_0:
                    switch (screenOrientation) {
                        case ORIENTATION_90:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_90;
                            break;
                        case ORIENTATION_180:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_180;
                            break;
                        case ORIENTATION_270:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_270;
                            break;
                        case ORIENTATION_0:
                            // no rotation
                            break;
                    }
                    break;
                case ORIENTATION_90:

                    switch (screenOrientation) {
                        case ORIENTATION_180:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_90;
                            break;
                        case ORIENTATION_270:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_180;
                            break;
                        case ORIENTATION_0:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_270;
                            break;
                        case ORIENTATION_90:
                            // no rotation
                            break;
                    }

                    break;
                case ORIENTATION_180:

                    switch (screenOrientation) {
                        case ORIENTATION_270:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_90;
                            break;
                        case ORIENTATION_0:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_180;
                            break;
                        case ORIENTATION_90:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_270;
                            break;
                        case ORIENTATION_180:
                            // no rotation
                            break;
                    }

                    break;
                case ORIENTATION_270:

                    switch (screenOrientation) {
                        case ORIENTATION_0:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_90;
                            break;
                        case ORIENTATION_90:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_180;
                            break;
                        case ORIENTATION_180:
                            rotation = ScreenOrientationSensor.ScreenOrientation.ORIENTATION_270;
                            break;
                        case ORIENTATION_270:
                            // no rotation
                            break;
                    }
                    break;
            }
        }

        switch (rotation) {
            case ORIENTATION_0:
                x = point.timedPoint.getX();
                y = point.timedPoint.getY();
                break;
            case ORIENTATION_90:
                x = point.timedPoint.getY() * ((float) canvasWidth) / point.canvasHeight;
                y = canvasHeight - point.timedPoint.getX() * ((float) canvasHeight) / point.canvasWidth;
                break;
            case ORIENTATION_180:
                x = canvasWidth - point.timedPoint.getX();
                y = canvasHeight - point.timedPoint.getY();
                break;
            case ORIENTATION_270:
                x = canvasWidth - point.timedPoint.getY() * ((float) canvasWidth) / point.canvasHeight;
                y = point.timedPoint.getX() * ((float) canvasHeight) / point.canvasWidth;
                break;
        }

        return new TouchTimedPoint(x, y, point.touchEventAction, point.timedPoint.getTimestamp(), screenOrientation, canvasWidth, canvasHeight);
    }

    private void recalculateDrawingTime() {
        currentDrawingTime = 0;
        TouchTimedPoint point;
        TouchTimedPoint lastPoint = null;
        for (int i = 0, size = touchTimedPoints.size(); i < size; i++) {
            point = touchTimedPoints.get(i);

            switch (point.touchEventAction) {
                case MotionEvent.ACTION_DOWN:
                    if (lastPoint != null) {
                        currentDrawingTime += 100;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    if (lastPoint != null) {
                        currentDrawingTime += point.timedPoint.timeTo(lastPoint.timedPoint);
                    }
                    break;
            }
            lastPoint = point;
        }
        notifyDrawingProgressChanged();
    }

    private void notifyDrawingProgressChanged() {
        if (listener != null) {
            listener.onDrawingTimeProgressChanged(getDrawingProgress());
        }
    }
}
