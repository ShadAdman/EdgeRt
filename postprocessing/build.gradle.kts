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
            implementation(project(":edgert"))
        }
    }
}

android {
    namespace = "org.kmp.playground.edgert.postprocessing"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
