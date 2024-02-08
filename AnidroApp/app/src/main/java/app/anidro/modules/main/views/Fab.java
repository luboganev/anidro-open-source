package app.anidro.modules.main.views;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A custom implementation of a {@link FloatingActionButton} which can be shown and hidden
 * with animation.
 * <p/>
 * Created by luboganev on 23/08/15.
 */
public class Fab extends FloatingActionButton {

    private ShowHideAnimator animator;

    public Fab(Context context) {
        super(context);
        init();
    }

    public Fab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Fab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        animator = new ShowHideAnimator(this);
    }

    /**
     * Shows the FAB.
     */
    @Override
    public void show() {
        animator.show(true);
    }

    /**
     * Shows the FAB.
     */
    public void show(boolean animate) {
        animator.show(animate);
    }

    /**
     * Hides the FAB.
     */
    @Override
    public void hide() {
        animator.hide(true);
    }

    /**
     * Hides the FAB.
     */
    public void hide(boolean animate) {
        animator.hide(animate);
    }
}