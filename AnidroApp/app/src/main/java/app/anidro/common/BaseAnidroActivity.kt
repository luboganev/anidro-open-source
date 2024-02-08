package app.anidro.common

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import app.anidro.R
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent

abstract class BaseAnidroActivity : AppCompatActivity() {

    /**
     * The singleton Navigator object of the application
     */
    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.setActivity(this)
    }

    override fun onStart() {
        super.onStart()
        // Make sure to update the navigator's current activity
        navigator.setActivity(this)
    }

    override fun onResume() {
        super.onResume()
        // Make sure to update the navigator's current activity
        navigator.setActivity(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        findAndAttachToolbar()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        findAndAttachToolbar()
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        super.setContentView(view, params)
        findAndAttachToolbar()
    }

    /**
     * Look for Toolbar in the layout, set is as an action bar and call the
     * children methods for customizing it
     */
    private fun findAndAttachToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            customizeToolbar(toolbar)
            setSupportActionBar(toolbar)
            customizeActionBar(supportActionBar!!)
        }
    }

    /**
     * This method gets called in case there is a toolbar to be attached as the activity actionbar
     *
     * @param toolbar
     * The attached action bar as [Toolbar]
     */
    protected open fun customizeToolbar(toolbar: Toolbar) {}

    /**
     * This method gets called after a toolbar has been attached as actionbar of the activity.
     *
     * @param actionBar
     * The attached action bar as [ActionBar]
     */
    protected open fun customizeActionBar(actionBar: ActionBar) {}
}