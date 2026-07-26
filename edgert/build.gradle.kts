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


    iosArm64(); iosSimulatorArm64()

    cocoapods {
        summary = "Core abstractions and native dependencies for EdgeRt"
        homepage = "https://github.com/ShadAdman/edgert"
        version = "1.0"
        ios.deploymentTarget = "17.0"
        podfile = project.file("../sample/iosApp/Podfile")

        pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
        pod("TensorFlowLiteObjC/Metal") { linkOnly = true }
        pod("TensorFlowLiteObjC/CoreML") { linkOnly = true }

        framework {
            baseName = "edgert"
            isStatic = true
            linkerOpts(
                project.file("../sample/iosApp/Pods/TensorFlowLiteObjC/Frameworks").path.let { "-F$it" },
                "-framework", "TensorFlowLiteObjC",
                "-Wl,-all_load"
            )
        }
    }

    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("17.0")
        swiftPackage(
            url = url("https://github.com/pytorch/executorch.git"),
            version = branch("swiftpm-1.3.1"),
            products = listOf(product("executorch"))
        )
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
            implementation(libs.executorch)
            implementation(libs.androidx.startup)
        }
    }
}

android {
    namespace = "org.kmp.playground.edgert"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
