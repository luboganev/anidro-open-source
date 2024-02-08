package app.anidro.modules.main.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import app.anidro.R;

/**
 * A simple view which represents a custom progress bar
 * <p/>
 * Created by luboganev on 29/08/15.
 */
public class CustomDrawableProgressBar extends AppCompatImageView {

    /**
     * @see <a href="http://developer.android.com/reference/android/graphics/drawable/ClipDrawable.html">ClipDrawable</a>
     */
    private static final double MAX = 10000.0d;

    public CustomDrawableProgressBar(Context context) {
        super(context);
        initCustomView();
    }

    public CustomDrawableProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomView();
    }

    public CustomDrawableProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomView();
    }

    private void initCustomView() {
        setImageResource(R.drawable.max_drawing_time_progress);
        setCurrentPercent(0);
    }

    public void setCurrentPercent(int percent) {
        setImageLevel((int)(((double)percent / 100.0d) * MAX));
    }
}
