plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    androidTarget()
    iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
        }
    }
}

android {
    namespace = "org.kmp.playground.kflite.postprocessing"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
