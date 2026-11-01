rootProject.name = "Cytosis"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.foxikle.dev/cytonic")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":protocol")
include(":launcher")
include(":nativeimage")