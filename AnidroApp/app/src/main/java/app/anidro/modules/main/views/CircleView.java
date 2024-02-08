package app.anidro.modules.main.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import app.anidro.common.DrawingColorsPalette;
import app.anidro.common.DrawingPaintBuilder;

/**
 * A simple view which draws a circle with specific color and diameter
 *
 * Created by luboganev on 23/01/16.
 */
public class CircleView extends View {
    private @ColorInt int circleColor;
    private float circleDiameterPixels;
    private Paint fillPaint;
    private Paint strokePaint;

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            circleColor = 0xFF3F51B5;
            circleDiameterPixels = 48;
        } else {
            circleColor = new DrawingColorsPalette(getResources()).getDefaultDrawingColor();
            circleDiameterPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    DrawingPaintBuilder.DEFAULT_STROKE_WIDTH_DP, getResources().getDisplayMetrics());
        }

        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(circleColor);

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(1.0F);
        strokePaint.setColor(getDarkerColor(circleColor));
    }

    public void setColorAndSize(@ColorInt int color, int circleDiameter) {
        circleColor = color;
        fillPaint.setColor(circleColor);
        strokePaint.setColor(getDarkerColor(circleColor));
        circleDiameterPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, circleDiameter, getResources().getDisplayMetrics());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = ((float)getWidth()) / 2.0F;
        float centerY = ((float)getHeight()) / 2.0F;
        canvas.drawCircle(centerX, centerY, circleDiameterPixels / 2.0F, fillPaint);
        if (circleDiameterPixels > 2.0F) {
            canvas.drawCircle(centerX, centerY, circleDiameterPixels / 2.0F, strokePaint);
        }
    }

    private @ColorInt int getDarkerColor(@ColorInt int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        hsl[2] = hsl[2] / 1.5F;
        return ColorUtils.HSLToColor(hsl);
    }
}
