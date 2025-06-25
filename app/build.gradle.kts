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
    
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    // Glide for image/video loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // ExoPlayer for video playback
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation ("com.squareup.okhttp3:okhttp:3.0.1")
    // CircleImageView for circular profile image
    implementation("com.github.hdodenhof:CircleImageView:3.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}