plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.test3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.test3"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    // ViewModel and LiveData
    implementation (libs.lifecycle.viewmodel)
    implementation (libs.lifecycle.livedata)

    // OkHttp
    implementation(libs.okhttp)
    // Gson for JSON parsing
    implementation(libs.gson)
    // Glide for image/video loading
    implementation(libs.glide)
    // ExoPlayer for video playback
    implementation(libs.exoplayer)
    implementation (libs.okhttp.v301)
    // CircleImageView for circular profile image
    implementation(libs.circleimageview)
    implementation (libs.material)
    // Retrofit for networking
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}