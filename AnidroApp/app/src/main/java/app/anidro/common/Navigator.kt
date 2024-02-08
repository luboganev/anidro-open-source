package app.anidro.common

import android.app.Activity
import android.content.Intent
import app.anidro.modules.settings.SettingsActivity

/**
 * This class encapsulates the necessary boilerplate code for navigating from one activity to another
 * or for launching specific intents requiring activity.
 */
class Navigator {
    private var activity: Activity? = null

    /**
     * This method should be called wheneve the foreground activity changes, so that the [Navigator]
     * always contains the latest activity which the user is interacting with
     */
    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    /**
     * Navigates to the [SettingsActivity]
     */
    fun navigateToSettings() {
        activity?.let { it.startActivity(Intent(it, SettingsActivity::class.java)) }
    }

    /**
     * Finishes the current foreground activity
     */
    fun finishCurrentActivity() {
        activity?.finish()
    }

    /**
     * Starts a new activity with a share [Intent]. This will most probably show a chooser to the user.
     */
    fun startActivityWithShareIntent(shareIntent: Intent?) {
        activity?.startActivity(shareIntent)
    }
}