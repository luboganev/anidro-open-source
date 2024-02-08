package app.anidro.modules.application

import app.anidro.common.Navigator
import app.anidro.modules.export.DrawingExporter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single {
        androidContext().resources
    }

    single {
        Navigator()
    }

    single {
        DrawingExporter(
                applicationContext = androidContext(),
                settingsPersistence = get())
    }
}