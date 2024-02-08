package app.anidro.modules.application

import android.app.Application
import app.anidro.BuildConfig
import app.anidro.common.CrashReportingTree
import app.anidro.modules.main.mainModule
import app.anidro.modules.main.mainPresenterModule
import app.anidro.modules.persistence.persistenceModule
import app.anidro.modules.persistence.settings.SettingsPersistence
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

class AnidroApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        Timber.plant(CrashReportingTree())

        val koin = startKoin {
            androidContext(this@AnidroApplication)
            modules(persistenceModule, appModule, mainModule, mainPresenterModule)
        }.koin

        // Perform persistence migrations if necessary
        koin.get<SettingsPersistence>().migrateVersion()
    }
}