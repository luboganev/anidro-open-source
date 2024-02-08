object Sdk {
    const val MIN_SDK_VERSION = 16
    const val TARGET_SDK_VERSION = 30
    const val COMPILE_SDK_VERSION = 30
    const val BUILD_TOOLS_VERSION = "30.0.2"
}

object BuildPluginsVersion {
    const val AGP = "4.0.0"
    const val KOTLIN = "1.4.0"
}

object App {
    const val APP_ID = "app.anidro"

    const val APP_VERSION_NAME = "1.7.1"
    const val APP_VERSION_CODE = 182
}

object AndroidX {
    const val core = "androidx.core:core-ktx:1.3.1"
    const val appCompat = "androidx.appcompat:appcompat:1.2.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val preference = "androidx.preference:preference:1.1.0"
    val lifeCycle = LifeCycle
    const val materialComponents = "com.google.android.material:material:1.2.0"
}

object LifeCycle {
    private const val version = "2.2.0"
    const val runtime = "androidx.lifecycle:lifecycle-runtime:$version"
    const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
    const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
}