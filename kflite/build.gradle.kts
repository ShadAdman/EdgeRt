import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    iosX64(); iosArm64(); iosSimulatorArm64()

    cocoapods {
        summary = "Core abstractions and native dependencies for KFlite"
        homepage = "https://github.com/ShadAdman/kflite"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../sample/iosApp/Podfile")

        pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
        pod("TensorFlowLiteObjC/Metal") { linkOnly = true }
        pod("TensorFlowLiteObjC/CoreML") { linkOnly = true }
        pod("LibTorch-Lite")
        pod("executorch")

        framework {
            baseName = "kflite"
            isStatic = true
            linkerOpts(
                project.file("../sample/iosApp/Pods/TensorFlowLiteObjC/Frameworks").path.let { "-F$it" },
                "-framework", "TensorFlowLiteObjC",
                "-framework", "LibTorch-Lite",
                "-framework", "executorch"
            )
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core abstractions
        }
        androidMain.dependencies {
            implementation(libs.litert)
            implementation("com.google.ai.edge.litert:litert-gpu:1.4.2") {
                exclude(group = "com.google.ai.edge.litert", module = "litert-api")
            }

            implementation("org.pytorch:pytorch_android:2.1.0")
//            implementation("org.pytorch:executorch-android:1.3.1")
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
