package app.anidro.modules.persistence.settings

import app.anidro.BuildConfig

/**
 * A simple persistence which stores the settings available for the user to choose
 *
 *
 * Created by luboganev on 13/12/15.
 */
interface SettingsPersistence {
    interface ChangeListener {
        fun onCopyToExternalSettingChanged(copyToExternal: Boolean)
        fun onShowExportNotificationSettingChanged(showExportNotification: Boolean)
    }

    fun setChangeListener(listener: ChangeListener?)
    fun setCopyToExternal(copyToExternal: Boolean)
    fun shouldCopyToExternal(): Boolean
    fun shouldShowExportNotification(): Boolean
    fun migrateVersion()

    companion object {
        // Shared preferences name
        const val SHARED_PREFERENCES_FILENAME = BuildConfig.APPLICATION_ID + "_settings"

        // Have to be the same as in the settings.xml file
        const val KEY_COPY_TO_EXTERNAL = "copy_to_external"
        const val KEY_SHOW_EXPORT_NOTIFICATION = "show_notifications"
    }
}