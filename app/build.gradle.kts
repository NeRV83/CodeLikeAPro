plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "ru.netology.nmedia"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ru.netology.nmedia"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["usesCleartextTraffic"] = false
            buildConfigField("String", "BASE_URL", "\"https://netology.ru/\"")
        }
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = true
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:9999/api/slow/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.gson)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    // Source: https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation(libs.okhttp3.okhttp)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.glide)
    implementation(libs.retrofit)
    // Source: https://mvnrepository.com/artifact/com.squareup.okhttp3/logging-interceptor
    implementation(libs.logging.interceptor)
    // Source: https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson
    implementation(libs.converter.gson)
    // Source: https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-android
    runtimeOnly(libs.kotlinx.coroutines.android)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}