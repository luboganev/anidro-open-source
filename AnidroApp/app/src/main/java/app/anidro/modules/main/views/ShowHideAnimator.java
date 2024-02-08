package app.anidro.modules.main.views;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;

import app.anidro.R;

/**
 * A view animator which can perform a show and hide animation
 * through changing view's size
 * <p/>
 * Created by luboganev on 31/08/15.
 */
public class ShowHideAnimator {
    private static final int ANIM_DURATION = 200;

    private final View animatedView;

    public ShowHideAnimator(View animatedView) {
        this.animatedView = animatedView;
    }

    /**
     * Shows the View.
     */
    public void show(boolean animate) {
        // Only use scale animation if FAB is hidden
        if (animatedView.getVisibility() != View.VISIBLE && animate) {
            // Animate FAB expanding
            ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(ANIM_DURATION);
            anim.setInterpolator(getInterpolator());
            animatedView.startAnimation(anim);
        }
        animatedView.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the View.
     */
    public void hide(boolean animate) {
        // Only use scale animation if FAB is visible
        if (animatedView.getVisibility() == View.VISIBLE && animate) {
            // Animate FAB shrinking
            ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(ANIM_DURATION);
            anim.setInterpolator(getInterpolator());
            animatedView.startAnimation(anim);
        }
        animatedView.setVisibility(View.INVISIBLE);
    }

    private Interpolator getInterpolator() {
        return AnimationUtils.loadInterpolator(animatedView.getContext(), R.interpolator.show_hide_interpolator);
    }
}
