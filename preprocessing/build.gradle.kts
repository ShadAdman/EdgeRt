plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    androidTarget()
    iosArm64(); iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":edgert"))
            implementation(compose.ui)
        }
    }
}

android {
    namespace = "org.kmp.playground.edgert.preprocessing"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
