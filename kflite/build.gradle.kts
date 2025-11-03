import com.vanniktech.maven.publish.SonatypeHost.Companion.CENTRAL_PORTAL
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
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
        summary = providers.gradleProperty("POM_DESCRIPTION").get()
        homepage = providers.gradleProperty("POM_URL").get()
        version = libs.versions.snapshotVersion.get()
        ios.deploymentTarget = libs.versions.iosDeploymentTarget.get()
        podfile = project.file("../iosApp/Podfile")

        pod("TensorFlowLiteObjC", moduleName = "TFLTensorFlowLite")
        pod("TensorFlowLiteObjC/Metal") {
            linkOnly = true
        }
        pod("TensorFlowLiteObjC/CoreML") {
            linkOnly = true
        }


        framework {
            baseName = "kflite"
            isStatic = true
            linkerOpts(
                project.file("../iosApp/Pods/TensorFlowLiteObjC/Frameworks").path.let { "-F$it" },
                "-framework", "TensorFlowLiteObjC"
            )
        }
    }


    sourceSets {
        androidMain.dependencies {
            implementation(compose.ui)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.startup)
            implementation(libs.tflite)
            implementation(libs.tfliteGPU)
            implementation(libs.tfliteGpuApi)
        }
        commonMain.dependencies {
            implementation(compose.ui)
        }
    }

}

android {
    namespace = providers.gradleProperty("ANDROID_NAMESPACE").get()
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
    val tag = System.getenv("GITHUB_REF")?.substringAfterLast("/") ?: libs.versions.snapshotVersion.get()

    coordinates(
        groupId = libs.versions.groupId.get(),
        artifactId = libs.versions.artifactId.get(),
        version = tag
    )

    pom {
        name.set(providers.gradleProperty("POM_NAME").get())
        description.set(providers.gradleProperty("POM_DESCRIPTION").get())
        url.set(providers.gradleProperty("POM_URL").get())
        licenses {
            license {
                name.set(providers.gradleProperty("POM_LICENSE_NAME").get())
                url.set(providers.gradleProperty("POM_LICENSE_URL").get())
            }
        }
        scm {
            connection.set("scm:git:git://github.com/shadmanadman/kflite.git")
            developerConnection.set("scm:git:ssh://git@github.com/shadmanadman/kflite.git")
            url.set(providers.gradleProperty("POM_URL").get())
        }
        developers {
            developer {
                id.set(providers.gradleProperty("POM_DEVELOPER_ID").get())
                name.set(providers.gradleProperty("POM_DEVELOPER_NAME").get())
                email.set(providers.gradleProperty("POM_DEVELOPER_EMAIL").get())
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyId"),
        System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey"),
        System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")
    )
}


