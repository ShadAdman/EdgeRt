import com.vanniktech.maven.publish.SonatypeHost.Companion.CENTRAL_PORTAL
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.vanniktechPublish)
    id("signing")
}

kotlin {

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()


    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
        framework {
            baseName = "kflite"
            isStatic = true
            linkerOpts(
                project.file("../iosApp/Pods/TensorFlowLiteC/Frameworks").path.let { "-F$it" },
                "-framework", "TensorFlowLiteC"
            )
        }
    }


    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.tflite)
            implementation(libs.tfliteGPU)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
    }

}

android {
    namespace = "org.kmp.playground.kflite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.android.targetSdk.get().toInt()
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

mavenPublishing {
    signAllPublications()

    publishToMavenCentral(CENTRAL_PORTAL)
    val tag: String? = System.getenv("GITHUB_REF")?.split("/")?.lastOrNull()

    coordinates(
        groupId = libs.versions.groupId.get(),
        artifactId = libs.versions.artifactId.get(),
        version = tag ?: "1.42.0-SNAPSHOT"
    )

    pom {
        name = "Kflite"
        description =
            "A Kotlin Multiplatform library to run TensorFlow lite models on iOS and Android targets"
        url = "https://github.com/shadmanadman/kflite"
        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "shadmanadman"
                name = "Shadman Adman"
                email = "adman.shadman@gmail.com"
            }
        }
        scm {
            connection = "scm:git:https://github.com/shadmanadman/kflite"
            developerConnection = "scm:git:https://github.com/shadmanadman/kflite.git"
            url = "https://github.com/shadmanadman/kflite"
        }
    }
}

signing {
    val keyId = System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyId")
    val key = System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")
    val keyPassword = System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")
    useInMemoryPgpKeys(
        keyId,
        key,
        keyPassword
    )
}


