package app.anidro.modules.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import app.anidro.R
import app.anidro.common.BaseAnidroActivity
import app.anidro.common.viewBinding
import app.anidro.databinding.ActivityAboutBinding

class AboutActivity : BaseAnidroActivity() {
    private val viewBinding by viewBinding(ActivityAboutBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.run {
            setContentView(root)
            aboutTerms.setOnClickListener { onClickTerms() }
            aboutPrivacy.setOnClickListener { onClickPrivacy() }
        }
    }

    override fun customizeActionBar(actionBar: ActionBar) {
        super.customizeActionBar(actionBar)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun customizeToolbar(toolbar: Toolbar) {
        super.customizeToolbar(toolbar)
        toolbar.setTitle(R.string.title_activity_about)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun onClickTerms() {
        openUrl("http://luboganev.github.io/anidro/terms/")
    }

    private fun onClickPrivacy() {
        openUrl("http://luboganev.github.io/anidro/privacy/")
    }

    private fun openUrl(url: String) {
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_about_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val appLink = "https://play.google.com/store/apps/details?id=app.anidro"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_share_subject))
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_text) + " " + appLink)
                if (shareIntent.resolveActivity(packageManager) != null) {
                    startActivity(shareIntent)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}