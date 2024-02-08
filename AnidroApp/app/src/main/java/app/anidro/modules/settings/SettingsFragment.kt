package app.anidro.modules.settings

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import app.anidro.R
import app.anidro.modules.persistence.settings.SettingsPersistence
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat(), SettingsPersistence.ChangeListener {

    private val notificationManager by lazy {
        requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val settingsPersistence: SettingsPersistence by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsPersistence.SHARED_PREFERENCES_FILENAME
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onResume() {
        super.onResume()
        settingsPersistence.setChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        settingsPersistence.setChangeListener(null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                resetCopyToExternalSetting()
                Toast.makeText(requireContext(), R.string.settings_storage_permission_denied, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCopyToExternalSettingChanged(copyToExternal: Boolean) {
        if (copyToExternal) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onShowExportNotificationSettingChanged(showExportNotification: Boolean) {
        if (!showExportNotification) {
            notificationManager.cancelAll()
        }
    }

    private fun resetCopyToExternalSetting() {
        findPreference<SwitchPreferenceCompat>(SettingsPersistence.KEY_COPY_TO_EXTERNAL)?.isChecked = false
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 42
    }
}