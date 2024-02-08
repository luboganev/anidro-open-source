package app.anidro.modules.persistence.terms

/**
 * A simple persistence which stores whether the user
 * has agreed with the terms and privacy before starting to
 * use the app.
 */
interface AcceptTermsPersistence {
    fun shouldAskToAcceptTerms(): Boolean
    fun hasAcceptedTerms()
}