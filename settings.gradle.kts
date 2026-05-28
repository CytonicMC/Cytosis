rootProject.name = "Cytosis"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.foxikle.dev/cytonic")
        maven("https://repo.minestom-united.dev/snapshots")
    }
}

include(":protocol")