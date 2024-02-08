package app.anidro.common

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * This class represents a sensor capable of listening for device orientation changes. It processes
 * the raw rotation of the devices and triggers events for device flip, when a transition between
 * portrait and reverse portrait, or landscape and reverse landscape occurs
 */
class ScreenOrientationFlipSensor(context: Context) {

    /**
     * Holds the current orientation of the screen. It may change often depending on device movement.
     */
    private var currentOrientation = Surface.ROTATION_0

    /**
     * Holds the timestamp of the last change of the [.currentOrientation]
     */
    private var currentOrientationChangeTimestamp = System.currentTimeMillis()

    /**
     * Holds the last orientation that remained unchanged for longer than the [.ORIENTATION_SETTLE_TIME]
     */
    private var lastSettledOrientation = Surface.ROTATION_0

    /**
     * Weak reference to the [OnScreenOrientationFlippedListener] to prevent memory leaks
     */
    private var weakListener = WeakReference<OnScreenOrientationFlippedListener>(null)

    /**
     * The raw framework orientation listener, which delivers the orientation in degrees (0-359)
     */
    private val orientationEventListener: OrientationEventListener = object : OrientationEventListener(context) {

        /**
         * Threshold for rotation in degrees
         */
        private val ROTATION_THRESHOLD = 10

        /**
         * Last rotation which this callback received
         */
        private var lastRotationDegrees = 0

        /**
         * A helper variable, declared as a field to prevent declaring it every time inside the callback
         */
        private var delta = 0

        override fun onOrientationChanged(orientationDegrees: Int) {
            // Only if a proper orientation is delivered
            if (orientationDegrees != ORIENTATION_UNKNOWN) {
                delta = abs(lastRotationDegrees - orientationDegrees)
                // Trying to reduce the sensitivity
                // but not when there is a new still unsettled orientation
                if (delta > ROTATION_THRESHOLD && delta < (360 - ROTATION_THRESHOLD)
                        || lastSettledOrientation != currentOrientation) {
                    lastRotationDegrees = orientationDegrees;
                    processNewOrientationDegrees(getPossibleOrientationFromDegrees(orientationDegrees));
                }
            }
        }
    }

    /**
     * Activates the sensor and sets its listener
     */
    fun enableSensor(listener: OnScreenOrientationFlippedListener) {
        if (orientationEventListener.canDetectOrientation()) {
            weakListener = WeakReference(listener)
            orientationEventListener.enable()
        }
    }

    /**
     * Deactivates the sensor and cleans up its listener
     */
    fun disableSensor() {
        orientationEventListener.disable()
        weakListener = WeakReference<OnScreenOrientationFlippedListener>(null)
    }

    /**
     * Processes the orientation resulting from a callback to the [.orientationEventListener]
     */
    private fun processNewOrientationDegrees(newOrientation: Int) {
        if (newOrientation != currentOrientation) {
            // we have an unsettled new orientation
            currentOrientationChangeTimestamp = System.currentTimeMillis()
            currentOrientation = newOrientation
        } else {
            if (System.currentTimeMillis() - currentOrientationChangeTimestamp > ORIENTATION_SETTLE_TIME) {
                // The new orientation settled
                if (isScreenOrientationFlip()) {
                    // It is a flip orientation change, notify the listener if possible
                    weakListener.get()?.onScreenOrientationFlipped()
                }
                // Update the last settled orientation
                lastSettledOrientation = currentOrientation
            }
        }
    }

    /**
     * Returns whether an orientation change can be considered as a flip, comparing the
     * [currentOrientation] with the [lastSettledOrientation]
     */
    private fun isScreenOrientationFlip(): Boolean {
        return when (lastSettledOrientation) {
            Surface.ROTATION_0 -> currentOrientation == Surface.ROTATION_180
            Surface.ROTATION_90 -> currentOrientation == Surface.ROTATION_270
            Surface.ROTATION_180 -> currentOrientation == Surface.ROTATION_0
            Surface.ROTATION_270 -> currentOrientation == Surface.ROTATION_90
            else -> false
        }
    }

    /**
     * Gets the possible orientation calculated from the orientation in degrees.
     */
    private fun getPossibleOrientationFromDegrees(orientationDegrees: Int) =
            when (orientationDegrees) {
                in 45 until 135 -> Surface.ROTATION_90
                in 135 until 225 -> Surface.ROTATION_180
                in 225 until 315 -> Surface.ROTATION_270
                else -> Surface.ROTATION_0
            }

    /**
     * A listener that gets invoked when a device flips its orientation.
     * A flip is the transition between either portrait and reverse portrait,
     * or landscape and reverse landscape.
     */
    interface OnScreenOrientationFlippedListener {
        /**
         * Called when the device orientation is flipped. This signifies a transition between either
         * portrait and reverse portrait, or landscape and reverse landscape.
         */
        fun onScreenOrientationFlipped()
    }

    companion object {
        /**
         * The minimum amount of time a particular orientation remains, before it is defined
         * as settled
         */
        private const val ORIENTATION_SETTLE_TIME: Long = 500
    }
}