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
            implementation(project(":kflite"))
        }
    }
}

android {
    namespace = "org.kmp.playground.kflite.coldstart"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

mavenPublishing {
    coordinates(
        artifactId = "coldstart"
    )
}
