import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.vanniktechPublish)
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()

    val nativeTargets = listOf(
        "linuxX64",
        "mingwX64",
        "macosX64",
        "macosArm64",
        "iosArm64",
        "iosX64",
        "iosSimulatorArm64"
    )

    targets.filter { it.name in nativeTargets }.forEach { target ->
        val platform = when {
            target.name.startsWith("linux") -> "linux"
            target.name.startsWith("mingw") -> "win"
            target.name.startsWith("macos") -> "mac"
            target.name == "iosArm64" -> "ios"
            else -> "ios-sim"
        }

        (target as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget).apply {
            compilations.getByName("main") {
                cinterops.create("litert") {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/litert.def"))
                    includeDirs(project.file("generated/$platform/include"))
                }
            }
            binaries.all {
                linkerOpts("-L${project.file("generated/$platform/lib").absolutePath}", "-llitert")
            }
        }
    }

    cocoapods {
        summary = "Core abstractions and native dependencies for KFlite"
        homepage = "https://github.com/ShadAdman/kflite"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../sample/iosApp/Podfile")

        pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
        pod("TensorFlowLiteObjC/Metal") { linkOnly = true }
        pod("TensorFlowLiteObjC/CoreML") { linkOnly = true }

        framework {
            baseName = "kflite"
            isStatic = true
            linkerOpts(
                project.file("../sample/iosApp/Pods/TensorFlowLiteObjC/Frameworks").path.let { "-F$it" },
                "-framework", "TensorFlowLiteObjC"
            )
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core abstractions
        }
        val nativeMain by getting
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
