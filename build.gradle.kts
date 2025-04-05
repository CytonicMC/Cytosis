import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `maven-publish`
    `java-library`
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.harbby.gradle.serviceloader") version ("1.1.9")
    id("dev.vankka.dependencydownload.plugin") version "1.3.1"
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.foxikle.dev/cytonic")
}

dependencies {
    compileOnlyApi("net.minestom:minestom-snapshots:0366b58bfe")
    compileOnlyApi("com.google.code.gson:gson:2.12.1") // serializing
    compileOnlyApi("com.squareup.okhttp3:okhttp:4.12.0") // http api requests
    compileOnlyApi("dev.hollowcube:polar:1.13.0") // Polar
    compileOnlyApi("redis.clients:jedis:5.2.0") // redis client
    compileOnlyApi("com.google.guava:guava:33.4.5-jre")

    compileOnly("org.projectlombok:lombok:1.18.36") // lombok
    annotationProcessor("org.projectlombok:lombok:1.18.36") // lombok

    runtimeDownload("net.kyori:adventure-text-minimessage:4.19.0")// better components
    runtimeDownload("com.mysql:mysql-connector-j:9.2.0") //mysql connector
    runtimeDownload("org.tomlj:tomlj:1.1.1") // Config lang
    runtimeDownload("org.reflections:reflections:0.10.2") // reflection utils
    runtimeDownload("org.slf4j:slf4j-api:2.0.17")  // SLF4J API
    runtimeDownload("org.apache.logging.log4j:log4j-core:2.24.3")  // Log4j core
    runtimeDownload("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
    runtimeDownload("io.nats:jnats:2.20.6")
    runtimeDownload("org.jooq:jooq:3.20.2") // database queries
    runtimeDownload("com.github.TogAr2:MinestomPvP:1b2f862baa") // pvp
    runtimeDownload("io.opentelemetry:opentelemetry-api:1.48.0")
    runtimeDownload("io.opentelemetry:opentelemetry-sdk:1.48.0")
    runtimeDownload("io.opentelemetry:opentelemetry-exporter-otlp:1.48.0")
    runtimeDownload("eu.koboo:minestom-invue:2025.1.1") {
        // we want to use our own, thank you :)
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }

    // the compileonlyapis need to be downloaded at runtime, too.
    runtimeDownloadOnly("net.minestom:minestom-snapshots:0366b58bfe")
    runtimeDownloadOnly("com.google.code.gson:gson:2.12.1")
    runtimeDownloadOnly("com.squareup.okhttp3:okhttp:4.12.0")
    runtimeDownloadOnly("dev.hollowcube:polar:1.13.0")
    runtimeDownloadOnly("redis.clients:jedis:5.2.0")
    runtimeDownloadOnly("com.google.guava:guava:33.4.5-jre")

    // Dependency loading
    implementation("dev.vankka:dependencydownload-runtime:1.3.1")
}

tasks.withType<Jar> {
    dependsOn(
        "generateRuntimeDownloadResourceForRuntimeDownloadOnly",
        "generateRuntimeDownloadResourceForRuntimeDownload"
    )
    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.bootstrap.Bootstrapper"
    }
}
tasks.withType<Javadoc> {
    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption("source", "21")
}

var bundled = false

val generateBuildInfo = tasks.register("generateBuildInfo") {
    dependsOn("incrementBuildNumber")

    val outputDir = file("$projectDir/build/generated/sources/buildinfo")
    val packageDir = File(outputDir, "net/cytonic/cytosis/utils")
    val buildInfoFile = File(packageDir, "BuildInfo.java")

    doLast {
        packageDir.mkdirs()
        outputDir.mkdirs()
        buildInfoFile.createNewFile()
        buildInfoFile.writeText(
            """
            package net.cytonic.cytosis.utils;
            
            public class BuildInfo {
                public static final String BUILD_VERSION = "${project.version}";
                public static final String BUILD_NUMBER = "$buildNumber";
                public static final String GIT_COMMIT = "${"git rev-parse --short HEAD".runCommand()}";
                public static final java.time.Instant BUILT_AT = java.time.Instant.ofEpochMilli(${System.currentTimeMillis()}L);
                public static final boolean DEPENDENCIES_BUNDLED=${bundled};
            }
            """.trimIndent()
        )
        println("Generated BuildInfo.java at $buildInfoFile")
    }
}

tasks {
    assemble {
        dependsOn("shadowJar")
        dependsOn("copyShadowJarToSecondary")
        dependsOn("copyForDocker")
    }
    named<ShadowJar>("shadowJar") {
        dependsOn(
            "generateRuntimeDownloadResourceForRuntimeDownloadOnly",
            "generateRuntimeDownloadResourceForRuntimeDownload"
        )
//        manifest {
//            attributes["Main-Class"] = "net.cytonic.cytosis.BootStrap"
//        }
        mergeServiceFiles()
        archiveFileName.set("cytosis.jar")
        archiveClassifier.set("")
        destinationDirectory.set(
            file(
                providers.gradleProperty("server_dir").orElse(destinationDirectory.get().toString())
            )
        )
    }
}

tasks.register<Copy>("copyForDocker") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.get().archiveFile)

    into(layout.buildDirectory.dir("libs"))
}

tasks.register<Copy>("copyShadowJarToSecondary") {
    dependsOn(tasks.shadowJar)

    if (providers.gradleProperty("server_dir2").isPresent) {
        from(tasks.shadowJar.get().archiveFile)
        into(providers.gradleProperty("server_dir2"))
    }
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

publishing {
    repositories {
        maven {
            name = "FoxikleCytonicRepository"
            url = uri("https://repo.foxikle.dev/cytonic")
//            credentials(PasswordCredentials::class)
            // Use providers to get the properties or fallback to environment variables
            var u = System.getenv("REPO_USERNAME")
            var p = System.getenv("REPO_PASSWORD")

            if (u == null || u.isEmpty()) {
                u = "no-value-provided"
            }
            if (p == null || p.isEmpty()) {
                p = "no-value-provided"
            }

            val user = providers.gradleProperty("FoxikleCytonicRepositoryUsername").orElse(u).get()
            val pass = providers.gradleProperty("FoxikleCytonicRepositoryPassword").orElse(p).get()
            credentials {
                username = user
                password = pass
            }
            authentication {
                create<BasicAuthentication>("basic") {

                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks["shadowJar"])
            artifact(javadocJar)
            artifact(sourcesJar)
        }
    }
}

val buildNumberFile = file("build-number.txt")

val buildNumber: Int = if (buildNumberFile.exists()) {
    buildNumberFile.readText().trim().toInt() + 1
} else {
    1
}

tasks.register("incrementBuildNumber") {
    doLast {
        buildNumberFile.writeText(buildNumber.toString())
        println("Build Number: $buildNumber")
    }
}

// Add generated source directory to Java compilation
tasks.compileJava {
    dependsOn(generateBuildInfo)
    source(generateBuildInfo.map { layout.buildDirectory.dir("generated/sources/buildinfo").get() })
}

sourceSets.main {
    java.srcDir(layout.buildDirectory.dir("generated/sources/buildinfo"))
}


project.extra["BUILD_NUMBER"] = buildNumber

// Helper function to run shell commands
fun String.runCommand(): String {
    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val process = if (isWindows) {
        ProcessBuilder("cmd", "/c", this)
    } else {
        ProcessBuilder("sh", "-c", this)
    }.start()

    return process.inputStream.bufferedReader().readText().trim()
}