package app.anidro.modules.persistence.terms

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import app.anidro.BuildConfig

class AcceptTermsSharedPrefPersistence(applicationContext: Context) : AcceptTermsPersistence {

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE)
    }

    override fun shouldAskToAcceptTerms(): Boolean {
        return !(sharedPreferences.getBoolean(KEY_HAS_ACCEPTED_TERMS, false)
                && sharedPreferences.getInt(KEY_TERMS_VERSION, -1) >= TERMS_VERSION)
    }

    @SuppressLint("ApplySharedPref")
    override fun hasAcceptedTerms() {
        sharedPreferences.edit()
                .putBoolean(KEY_HAS_ACCEPTED_TERMS, true)
                .putInt(KEY_TERMS_VERSION, TERMS_VERSION)
                .commit()
    }

    companion object {
        private const val TERMS_VERSION = 1
        private const val SHARED_PREFERENCES_FILENAME = BuildConfig.APPLICATION_ID + "_accept_terms"
        private const val KEY_HAS_ACCEPTED_TERMS = "has_accepted_terms"
        private const val KEY_TERMS_VERSION = "terms_version"
    }
}