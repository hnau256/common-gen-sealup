rootProject.name = "common-gen-sealup"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":annotations")
include(":processor")
include(":test")