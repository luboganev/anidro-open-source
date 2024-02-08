package app.anidro.modules.main

import android.content.Context
import android.content.res.Resources
import android.os.Vibrator
import app.anidro.R
import app.anidro.common.DisplayMetricsConverter
import app.anidro.common.DrawingColorsPalette
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mainModule = module {
    factory { DrawingColorsPalette(get()) }
    single { DisplayMetricsConverter(get()) }
    single { androidContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
}

val mainPresenterModule = module {
    single<MainPresenterInput> {
        val resources: Resources = get()
        MainPresenter(
                get(),
                get(),
                resources.getString(R.string.share_to),
                get(),
                get(),
                get(),
                get()
        )
    }
}