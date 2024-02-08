package app.anidro.modules.persistence

import app.anidro.modules.persistence.settings.SettingsPersistence
import app.anidro.modules.persistence.settings.SettingsSharedPrefPersistence
import app.anidro.modules.persistence.terms.AcceptTermsPersistence
import app.anidro.modules.persistence.terms.AcceptTermsSharedPrefPersistence
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val persistenceModule = module {

    single<SettingsPersistence> {
        SettingsSharedPrefPersistence(androidContext())
    }

    single<AcceptTermsPersistence> {
        AcceptTermsSharedPrefPersistence(androidContext())
    }
}