import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinCocoapods)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Core abstractions and native dependencies for KFlite"
        homepage = "https://github.com/ShadAdman/kflite"
        version = "1.0"
        ios.deploymentTarget = "16.0"

        pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
        pod("TensorFlowLiteObjC/Metal") { linkOnly = true }
        pod("TensorFlowLiteObjC/CoreML") { linkOnly = true }
    }

    sourceSets {
        commonMain.dependencies {
            // Core abstractions
        }
        androidMain.dependencies {
            api(libs.litert)
            api("com.google.ai.edge.litert:litert-gpu:1.4.2") {
                exclude(group = "com.google.ai.edge.litert", module = "litert-api")
            }
            implementation(libs.androidx.startup)
        }
    }
}

android {
    namespace = "org.kmp.playground.kflite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
