import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinCocoapods)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_22)
        }
    }
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    macosX64()
    macosArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "org.kmp.playground.kflite.sample")
        }
    }

    targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        .filter { it.name !in listOf("iosX64", "iosArm64", "iosSimulatorArm64") }
        .forEach { target ->
            target.binaries.executable {
                entryPoint = "org.kmp.playground.kflite.sample.main"
            }
        }

    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val hostTarget = when {
        hostOs == "Mac OS X" -> if (isArm64) "macosArm64" else "macosX64"
        hostOs == "Linux" -> "linuxX64"
        hostOs.startsWith("Windows") -> "mingwX64"
        else -> null
    }

    if (hostTarget != null) {
        tasks.register("runDesktop") {
            group = "run"
            dependsOn("runDebugExecutable${hostTarget.replaceFirstChar { it.uppercase() }}")
        }
    }

    cocoapods {
        summary = "KFlite Sample"
        homepage = "https://github.com/ShadAdman/kflite"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("iosApp/Podfile")

        pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
        pod("TensorFlowLiteObjC/Metal") { linkOnly = true }
        pod("TensorFlowLiteObjC/CoreML") { linkOnly = true }
    }

    sourceSets {
        val nativeMain by getting
        val jvmMain by getting
        val desktopMain by creating {
            dependsOn(nativeMain)
        }
        
        getting { dependsOn(desktopMain) } // linuxX64Main
        getting { dependsOn(desktopMain) } // mingwX64Main
        getting { dependsOn(desktopMain) } // macosX64Main
        getting { dependsOn(desktopMain) } // macosArm64Main

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":preprocessing"))
            implementation(project(":postprocessing"))
            
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "org.kmp.playground.kflite.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.kmp.playground.kflite.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "org.kmp.playground.kflite.sample.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "org.kmp.playground.kflite.sample"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
