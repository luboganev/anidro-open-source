package app.anidro.modules.settings

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import app.anidro.R
import app.anidro.common.BaseAnidroActivity

class SettingsActivity : BaseAnidroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
    }

    override fun customizeActionBar(actionBar: ActionBar) {
        super.customizeActionBar(actionBar)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun customizeToolbar(toolbar: Toolbar) {
        super.customizeToolbar(toolbar)
        toolbar.setTitle(R.string.title_activity_settings)
    }
}