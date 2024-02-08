package app.anidro.modules.main.views;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import app.anidro.R;

/**
 * A view animator which can perform a shake animation
 * <p/>
 * Created by luboganev on 31/08/15.
 */
public class ShakeAnimator {
    private final View animatedView;

    public ShakeAnimator(View animatedView) {
        this.animatedView = animatedView;
    }

    /**
     * Shakes the View.
     */
    public void shake() {
        // Only use shake if view is visible
        if (animatedView.getVisibility() == View.VISIBLE) {
            // Animate shake
            Animation anim = AnimationUtils.loadAnimation(animatedView.getContext(), R.anim.shake);
            animatedView.startAnimation(anim);
        }
    }
}
