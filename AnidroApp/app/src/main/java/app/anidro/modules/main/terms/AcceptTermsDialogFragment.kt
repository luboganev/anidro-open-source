package app.anidro.modules.main.terms

import android.app.Activity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import app.anidro.R
import app.anidro.common.viewBinding
import app.anidro.databinding.AcceptTermsDialogFragmentBinding

class AcceptTermsDialogFragment : DialogFragment() {

    private val viewBinding by viewBinding(AcceptTermsDialogFragmentBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.accept_terms_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.text.movementMethod = LinkMovementMethod.getInstance()
        viewBinding.acceptButton.setOnClickListener { onAcceptClicked() }
        viewBinding.declineButton.setOnClickListener { onDeclineClicked() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    private fun onAcceptClicked() {
        val parentActivity: Activity? = activity
        if (parentActivity != null) {
            if (parentActivity is AcceptTermsCallback) {
                (parentActivity as AcceptTermsCallback).onAcceptTerms()
            }
        }
        dismiss()
    }

    private fun onDeclineClicked() {
        val parentActivity: Activity? = activity
        if (parentActivity != null) {
            if (parentActivity is AcceptTermsCallback) {
                (parentActivity as AcceptTermsCallback).onDeclineTerms()
            }
        }
        dismiss()
    }

    companion object {
        const val TAG = "ASK_ACCEPT_TERMS_DIALOG"

        @JvmStatic
        fun isAlreadyShown(manager: FragmentManager): Boolean {
            return manager.findFragmentByTag(TAG) != null
        }
    }
}