rootProject.name = "Cytosis"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.foxikle.dev/cytonic")
        //todo add back when repo back up
//        maven("https://repo.minestom-united.dev/snapshots")
    }
}

include(":protocol")
include(":launcher")