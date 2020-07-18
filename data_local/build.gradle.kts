import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(Base.currentSDK)

    defaultConfig {
        minSdkVersion(Base.minSDK)
        targetSdkVersion(Base.currentSDK)
        versionCode = Base.versionCode
        versionName = Base.versionName
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))

    implementation(Deps.Androidx.room)
    implementation(Deps.Ktx.room)
    implementation(Deps.Google.dagger)
    kapt(Deps.Kapt.room)
    kapt(Deps.Kapt.dagger)

    testImplementation(Deps.Test.junit)
    androidTestImplementation(Deps.UiTest.junit)
    androidTestImplementation(Deps.UiTest.espresso)
}