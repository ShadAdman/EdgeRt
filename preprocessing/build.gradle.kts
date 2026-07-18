plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    jvm()
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
