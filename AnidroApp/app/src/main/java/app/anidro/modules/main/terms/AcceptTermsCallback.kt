package app.anidro.modules.main.terms

/**
 * A callback for the [AcceptTermsDialogFragment].
 * This should be implemented by the [android.app.Activity] which starts
 * the dialog fragment.
 */
interface AcceptTermsCallback {
    fun onAcceptTerms()
    fun onDeclineTerms()
}