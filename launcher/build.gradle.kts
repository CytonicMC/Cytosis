plugins {
    id("java")
    application
    id("com.gradleup.shadow") version "9.6.1"
    id("io.freefair.lombok") version "9.5.0"
    id("dev.minestom-united.minestom-events") version "0.0.2"
    alias(libs.plugins.blossom)
    alias(libs.plugins.indragit)
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.foxikle.dev/cytonic")
}

dependencies {
    implementation(project(":"))
}

minestomEvents {
    outputPackage = "net.cytonic.cytosis.launcher.utils"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.named<JavaExec>("run") {
    workingDir = file("run")
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("buildVersion", project.version.toString())
                property("gitCommit", indraGit.commit().get().name())
                properties.put("builtAt", System.currentTimeMillis())
            }
        }
    }
}

tasks {
    application {
        mainClass.set("net.cytonic.cytosis.launcher.CytosisMain")
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
