package app.anidro.modules.persistence.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

class SettingsSharedPrefPersistence(applicationContext: Context) : SettingsPersistence {

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SettingsPersistence.SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE)
    }

    private var changeListener: SettingsPersistence.ChangeListener? = null

    private val sharedPreferenceChangeListener: OnSharedPreferenceChangeListener = object : OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            val listener = changeListener
            if (listener != null) {
                if (key == SettingsPersistence.KEY_COPY_TO_EXTERNAL) {
                    listener.onCopyToExternalSettingChanged(sharedPreferences.getBoolean(key, false))
                }
                if (key == SettingsPersistence.KEY_SHOW_EXPORT_NOTIFICATION) {
                    listener.onShowExportNotificationSettingChanged(sharedPreferences.getBoolean(key, false))
                }
            } else {
                this@SettingsSharedPrefPersistence.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            }
        }
    }

    override fun setChangeListener(listener: SettingsPersistence.ChangeListener?) {
        changeListener = listener
        if (changeListener != null) {
            sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        } else {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        }
    }

    override fun setCopyToExternal(copyToExternal: Boolean) {
        sharedPreferences.edit().putBoolean(SettingsPersistence.KEY_COPY_TO_EXTERNAL, copyToExternal).apply()
    }

    override fun shouldCopyToExternal(): Boolean {
        return sharedPreferences.getBoolean(SettingsPersistence.KEY_COPY_TO_EXTERNAL, false)
    }

    override fun shouldShowExportNotification(): Boolean {
        return sharedPreferences.getBoolean(SettingsPersistence.KEY_SHOW_EXPORT_NOTIFICATION, false)
    }

    override fun migrateVersion() {
        val currentVersion = sharedPreferences.getInt(KEY_VERSION, -1)
        if (currentVersion == VERSION) {
            // nothing to migrate
            return
        }
        val editor = sharedPreferences.edit()
        if (currentVersion < 1) {
            // Automatically keep current behavior for devices below M
            editor.putBoolean(SettingsPersistence.KEY_COPY_TO_EXTERNAL, true)
            editor.putBoolean(SettingsPersistence.KEY_SHOW_EXPORT_NOTIFICATION, true)
        }
        editor.putInt(KEY_VERSION, VERSION).apply()
    }

    companion object {
        // Shared preferences keys
        private const val KEY_VERSION = "version"

        private const val VERSION = 1
    }
}