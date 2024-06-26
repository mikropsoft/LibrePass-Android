plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.medzik.librepass.android"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "dev.medzik.librepass.android"
        minSdk = libs.versions.android.sdk.min.get().toInt()
        targetSdk = libs.versions.android.sdk.target.get().toInt()
        versionCode = 17
        versionName = "1.3.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    lint {
        warning.add("MissingTranslation")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
}

dependencies {
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.ui)

    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.androidx.biometric)

    // used for calling `onResume` and locking vault after X minutes
    implementation(libs.compose.lifecycle.runtime)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // dagger
    implementation(libs.dagger.hilt)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.coil.compose)

    implementation(libs.librepass.client)
    implementation(libs.otp)

    // for restarting the application, for example, after changing the theme in settings
    implementation(libs.process.phoenix)

    implementation(libs.kotlinx.coroutines)

    implementation(libs.commons.lang3)

    implementation(project(":m3-pullrefresh"))

    // for splash screen with material3 and dynamic color
    implementation(libs.google.material)

    implementation(libs.zxing.android) { isTransitive = false }
    implementation(libs.zxing)

    implementation(libs.medzik.android.components)
    implementation(libs.medzik.android.crypto)
    implementation(libs.medzik.android.utils)

    // for testing
    debugImplementation(libs.compose.ui.test.manifest)

    // for preview support
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
}
