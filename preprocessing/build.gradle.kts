plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    androidTarget()
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64(); iosArm64(); iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
        }
    }
}

android {
    namespace = "org.kmp.playground.kflite.preprocessing"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}
