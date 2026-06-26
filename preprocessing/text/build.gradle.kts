plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget()
    iosX64(); iosArm64(); iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies { implementation(project(":core")) }
    }
}

android {
    namespace = "org.kmp.playground.kflite.preprocessing.text"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
