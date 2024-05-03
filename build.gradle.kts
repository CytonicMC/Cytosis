import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.harbby.gradle.serviceloader") version ("1.1.8")
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

//serviceLoader.serviceInterfaces.add("net.minestom.vanilla.VanillaReimplementation\$Feature")
serviceLoader.serviceInterfaces.add("org.slf4j.spi.SLF4JServiceProvider")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Minestom", "Minestom", "7daf8d69b7") // minstom itself
    implementation("com.google.code.gson:gson:2.10.1") // serializing
    implementation("org.slf4j:slf4j-api:1.7.36") // logging
    implementation("net.kyori:adventure-text-minimessage:4.16.0")// better components
    implementation("org.tomlj:tomlj:1.1.1") // Config lang
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.Cytosis"
    }
}
tasks.withType<JavaCompile> {
    // use String templates
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<JavaCompile> {
    // use String templates
    options.compilerArgs.add("--enable-preview")
}

tasks {
    assemble {
        dependsOn("shadowJar")
    }
    named<ShadowJar>("shadowJar") {
        manifest {
            attributes["Main-Class"] = "net.cytonic.cytosis.Cytosis"
        }
        mergeServiceFiles()
        archiveFileName.set("cytosis.jar")
    }
}