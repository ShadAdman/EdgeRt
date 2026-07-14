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

    sourceSets {
        commonMain.dependencies {
            // Core abstractions
        }
        val nativeMain by getting
        val desktopMain by creating {
            dependsOn(nativeMain)
        }
        val linuxMain by getting {
            dependsOn(desktopMain)
        }
        val macosMain by getting {
            dependsOn(desktopMain)
        }
        val mingwMain by getting {
            dependsOn(desktopMain)
        }

        androidMain.dependencies {
            api(libs.litert)
            api("com.google.ai.edge.litert:litert-gpu:1.4.2") {
                exclude(group = "com.google.ai.edge.litert", module = "litert-api")
            }
            implementation(libs.androidx.startup)
        }
    }

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
        val (platform, arch) = when (target.name) {
            "linuxX64" -> "linux" to "x64"
            "mingwX64" -> "win" to "x64"
            "macosX64" -> "mac" to "x64"
            "macosArm64" -> "mac" to "arm64"
            "iosArm64" -> "ios" to "arm64"
            "iosX64" -> "ios" to "x64"
            "iosSimulatorArm64" -> "ios" to "arm64"
            else -> "ios" to "arm64"
        }

        if (platform == "ios") return@forEach

        val defFile = when (platform) {
            "linux" -> "litert_linux.def"
            "mac" -> "litert_macos.def"
            "win" -> "litert_mingw.def"
            else -> "litert.def"
        }

        (target as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget).apply {
            compilations.getByName("main") {
                defaultSourceSet.dependsOn(this@kotlin.sourceSets.getByName("desktopMain"))
                cinterops.create("litert") {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/litert.def"))
                    includeDirs(project.file("generated/litert/$platform/$arch/include"))
                }
            }
            binaries.all {
                val libName = if (platform == "win") "LiteRt" else "LiteRt"
                linkerOpts("-L${project.file("generated/litert/$platform/$arch/lib").absolutePath}", "-l$libName")
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
