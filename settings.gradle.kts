enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "KfliteLib"
include(":kflite")
include(":preprocessing:image")
include(":preprocessing:audio")
include(":preprocessing:text")
include(":postprocessing:nms")
include(":postprocessing:coco")
include(":postprocessing:image")
include(":sample")
