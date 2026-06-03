plugins {
    id("java")
    application
    id("com.gradleup.shadow") version "9.4.2"
    id("io.freefair.lombok") version "9.5.0"
    id("dev.minestomunited.minestom-events") version "0.0.1-SNAPSHOT"
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.foxikle.dev/cytonic")
    maven("https://repo.minestom-united.dev/snapshots")
}

dependencies {
    implementation(project(":"))
}

minestomEvents {
    outputPackage = "net.cytonic.cytosis.utils"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.named<JavaExec>("run") {
    workingDir = file("run")
}

tasks {
    application {
        mainClass.set("net.cytonic.cytosis.CytosisMain")
    }
    shadowJar {
        archiveFileName.set("cytosis.jar")
        archiveClassifier.set("")
        mergeServiceFiles()
    }
    jar {
        enabled = false
    }
}
