plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    androidTarget()
    iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(compose.ui)
        }
    }
}

android {
    namespace = "org.kmp.playground.kflite.preprocessing"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
