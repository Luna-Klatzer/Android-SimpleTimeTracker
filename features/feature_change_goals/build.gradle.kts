import com.example.util.simpletimetracker.Base
import com.example.util.simpletimetracker.Deps
import com.example.util.simpletimetracker.applyAndroidLibrary

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

applyAndroidLibrary()

android {
    namespace = "${Base.namespace}.feature_change_goals"
}

dependencies {
    implementation(project(":feature_change_goals:api"))
    implementation(project(":core"))
    implementation(Deps.Google.dagger)
    kapt(Deps.Kapt.dagger)
}
