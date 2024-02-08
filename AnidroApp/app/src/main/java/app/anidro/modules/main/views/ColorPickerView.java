package app.anidro.modules.main.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * A custom color picker view which shows a
 * grid with colors for the user to pick from.
 * <p/>
 * Created by luboganev on 23/01/16.
 */
public class ColorPickerView extends View {
    private int[] colors;
    private int startIndex;
    private int count;
    private int lineCount;
    private int selectedRegionIndex;
    private Paint colorRegionPaint;
    private @Nullable OnColorSelectedCallbackListener onColorSelectedCallbackListener;

    public interface OnColorSelectedCallbackListener {
        void onColorSelected(@ColorInt int color, int index);
    }

    public ColorPickerView(Context context) {
        super(context);
        initView();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        startIndex = 0;
        count = 0;
        lineCount = 0;
        colors = new int[0];
        selectedRegionIndex = -1;
        colorRegionPaint = new Paint();
    }

    public void setOnColorSelectedCallbackListener(@Nullable OnColorSelectedCallbackListener onColorSelectedCallbackListener) {
        this.onColorSelectedCallbackListener = onColorSelectedCallbackListener;
    }

    /**
     * The view will display a single row with all colors from the input array.
     */
    public void setColors(@ColorInt int[] colors) {
        setColors(colors, 0, colors.length);
    }

    /**
     * The view will display a single row with a subset of the colors
     * from the input array starting from the input index.
     */
    public void setColors(@ColorInt int[] colors, int startIndex, int count) {
        setColors(colors, startIndex, count, 1);
    }

    /**
     * The view will display a subset of the colors
     * from the input array starting from the input
     * index and will distribute them on several lines
     */
    public void setColors(@ColorInt int[] colors, int startIndex, int count, int lineCount) {
        this.startIndex = startIndex;
        this.count = count;
        this.colors = colors;
        this.lineCount = lineCount;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (colors.length <= 0) {
            return false;
        }

        final float x = event.getX();
        final float y = event.getY();

        if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
            return false;
        }

        // Do calculations depending on the current configuration
        final int colorsPerLine = count / lineCount;
        final float colorRegionWidth = getWidth() / ((float)colorsPerLine);
        final float colorRegionHeight = getHeight() / ((float)lineCount);

        // Calculate which color region is the user touching
        int regionIndex = (int)(x / colorRegionWidth) + ((int)(y / colorRegionHeight) * colorsPerLine);

        // Check if such region exists at all
        if (regionIndex < 0 || regionIndex > colors.length - 1) {
            return false;
        }

        // Invoke the callback if the selected color changed
        if (regionIndex != selectedRegionIndex && onColorSelectedCallbackListener != null) {
            onColorSelectedCallbackListener.onColorSelected(colors[regionIndex + startIndex],
                    regionIndex + startIndex);
        }

        // Update the selected color
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                selectedRegionIndex = -1;
                break;
            default:
                selectedRegionIndex = regionIndex;
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            // some sample colors for the preview
            colors = new int[] {0xFFC5CAE9, 0xFF7986CB, 0xFF3F51B5, 0xFF303F9F, 0xFF1A237E };
            count = colors.length;
            startIndex = 0;
            lineCount = 1;
        }

        if (count <= 0) {
            return;
        }

        final int colorsPerLine = count / lineCount;

        final float colorRegionWidth = getWidth() / ((float)colorsPerLine);
        final float colorRegionHeight = getHeight() / ((float)lineCount);

        float top = 0.0F;
        float bottom = colorRegionHeight;
        float left = 0;

        int lineIndex = 0;
        for (int i = startIndex, size = startIndex + count; i < size; i++) {
            if (lineIndex == colorsPerLine) {
                lineIndex = 0;
                top += colorRegionHeight;
                bottom += colorRegionHeight;
                left = 0;
            }
            colorRegionPaint.setColor(colors[i]);
            canvas.drawRect(left, top, left + colorRegionWidth, bottom, colorRegionPaint);
            left += colorRegionWidth;
            lineIndex++;
        }
    }
}
