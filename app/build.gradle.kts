plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chattingappproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chattingappproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled=true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding=true

    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // for the application responsiveness :
    implementation(libs.sdp)
    implementation(libs.ssp)

    // The SSP and SDP libraries provide the scalable sp and dp units respectively

    // Using the library for rounded imageView
    implementation(libs.roundedimageview)

    //Necessary Firebase services
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.messaging)

    //implementing the Firebase dependencies
    implementation ("com.google.firebase:firebase-messaging:24.0.0")
    implementation ("com.google.firebase:firebase-firestore:25.0.0")

    //MultiDexing :
    implementation ("androidx.multidex:multidex:2.0.1")

    //Retrofit:
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
}
apply(plugin = "com.google.gms.google-services")