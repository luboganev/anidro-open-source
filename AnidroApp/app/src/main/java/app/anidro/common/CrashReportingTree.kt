package app.anidro.common

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * A [Timber] crash reporting tree which is used for sending error to Crashlytics
 * for the release build of the application
 */
class CrashReportingTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority == Log.ERROR
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR) {
            val instance = FirebaseCrashlytics.getInstance()
            instance.log(message)
            if (t == null) {
                instance.recordException(Throwable("Non-exception error: $message"))
            } else {
                instance.recordException(t)
            }
        }
    }
}