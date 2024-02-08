plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("com.google.firebase.crashlytics")
}

fun getFileProviderAuthority(applicationId: String) = "$applicationId.files"

fun getManifestPlaceholders(applicationId: String) = mapOf(
        "filesAuthority" to getFileProviderAuthority(applicationId)
)

android {
    compileSdkVersion(Sdk.COMPILE_SDK_VERSION)
    buildToolsVersion = Sdk.BUILD_TOOLS_VERSION

    defaultConfig {
        minSdkVersion(Sdk.MIN_SDK_VERSION)
        targetSdkVersion(Sdk.TARGET_SDK_VERSION)

        applicationId = App.APP_ID
        versionCode = App.APP_VERSION_CODE
        versionName = App.APP_VERSION_NAME

        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    signingConfigs.create("release") {
        storeFile = file("anidro-release.keystore")
        storePassword = ""
        keyAlias = ""
        keyPassword = ""
    }
    signingConfigs.getByName("debug") {
        storeFile = file("debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
            manifestPlaceholders = getManifestPlaceholders(App.APP_ID)

            buildConfigField("String", "FILES_AUTHORITY", "\"${getFileProviderAuthority(App.APP_ID)}\"")
            buildConfigField("boolean", "USE_CRASHLYTICS", "true")
            resValue("string", "GRADLE_APPLICATION_ID", "\"${App.APP_ID}\"")
        }
        getByName("debug") {
            signingConfig = signingConfigs.findByName("debug")
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false

            val fullApplicationId = "${App.APP_ID}.debug"
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"

            manifestPlaceholders = getManifestPlaceholders(fullApplicationId)
            buildConfigField("String", "FILES_AUTHORITY", "\"${getFileProviderAuthority(fullApplicationId)}\"")
            buildConfigField("boolean", "USE_CRASHLYTICS", "false")
            resValue("string", "GRADLE_APPLICATION_ID", "\"$fullApplicationId\"")
        }
    }

    lintOptions {
        isWarningsAsErrors = false
        isAbortOnError = true
    }

    // For Kotlin projects
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    androidExtensions {
        isExperimental = true
    }
}

dependencies {
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7")

    // Android Jetpack
    implementation(AndroidX.core)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.preference)
    implementation(AndroidX.lifeCycle.runtime)
    implementation(AndroidX.lifeCycle.commonJava8)
    implementation(AndroidX.lifeCycle.viewModel)
    implementation(AndroidX.lifeCycle.liveData)
    implementation(AndroidX.materialComponents)

    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Koin
    implementation("org.koin:koin-android:2.1.6")

    // Crashlytics
    implementation("com.google.firebase:firebase-crashlytics:17.2.1")
}

apply(plugin = "com.google.gms.google-services")
