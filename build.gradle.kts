import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `maven-publish`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version "8.3.6"
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
    compileOnlyApi("net.minestom:minestom-snapshots:1_21_5-aa17002536")
    compileOnlyApi("com.google.code.gson:gson:2.13.1") // serializing
    compileOnlyApi("com.squareup.okhttp3:okhttp:4.12.0") // http api requests
    compileOnlyApi("dev.hollowcube:polar:1.14.2") // Polar
    compileOnlyApi("redis.clients:jedis:6.0.0") // redis client
    compileOnlyApi("com.google.guava:guava:33.4.8-jre")
//    compileOnlyApi("com.github.TogAr2:MinestomPvP:-SNAPSHOT")
    compileOnlyApi("com.github.CodeDoctorDE:MinestomPvP:1_21_5-SNAPSHOT") // pvp
    compileOnlyApi("eu.koboo:minestom-invue:2025.1.1") {
        // we want to use our own, thank you :)
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }
    compileOnlyApi("org.spongepowered:configurate-gson:4.2.0")
    compileOnlyApi("io.github.classgraph:classgraph:4.8.179")

    compileOnly("org.projectlombok:lombok:1.18.38") // lombok
    annotationProcessor("org.projectlombok:lombok:1.18.38") // lombok

    runtimeDownload("net.kyori:adventure-text-minimessage:4.21.0")// better components
    runtimeDownload("com.mysql:mysql-connector-j:9.3.0") //mysql connector
    runtimeDownload("org.reflections:reflections:0.10.2") // reflection utils
    runtimeDownload("org.slf4j:slf4j-api:2.0.17")  // SLF4J API
    runtimeDownload("org.apache.logging.log4j:log4j-core:2.24.3")  // Log4j core
    runtimeDownload("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
    runtimeDownload("io.nats:jnats:2.21.2")
    runtimeDownload("org.jooq:jooq:3.20.4") // database queries
//    runtimeDownload("com.github.TogAr2:MinestomPvP:-SNAPSHOT") // pvp
    runtimeDownload("com.github.CodeDoctorDE:MinestomPvP:1_21_5-SNAPSHOT") // pvp
    runtimeDownload("io.opentelemetry:opentelemetry-api:1.51.0")
    runtimeDownload("io.opentelemetry:opentelemetry-sdk:1.51.0")
    runtimeDownload("io.opentelemetry:opentelemetry-exporter-otlp:1.51.0")
    runtimeDownload("eu.koboo:minestom-invue:2025.1.1") {
        // we want to use our own, thank you :)
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }

    // the compileonlyapis need to be downloaded at runtime, too.
    runtimeDownloadOnly("net.minestom:minestom-snapshots:1_21_5-aa17002536")
    runtimeDownloadOnly("com.google.code.gson:gson:2.13.1")
    runtimeDownloadOnly("com.squareup.okhttp3:okhttp:4.12.0")
    runtimeDownloadOnly("dev.hollowcube:polar:1.14.2")
    runtimeDownloadOnly("redis.clients:jedis:6.0.0")
    runtimeDownloadOnly("com.google.guava:guava:33.4.8-jre")
//    runtimeDownloadOnly("com.github.TogAr2:MinestomPvP:1b2f862baa")
    runtimeDownloadOnly("com.github.CodeDoctorDE:MinestomPvP:1_21_5-SNAPSHOT")
    runtimeDownloadOnly("eu.koboo:minestom-invue:2025.1.1")
    runtimeDownloadOnly("org.spongepowered:configurate-gson:4.2.0")
    runtimeDownloadOnly("io.github.classgraph:classgraph:4.8.179")

    // Dependency loading
    implementation("dev.vankka:dependencydownload-runtime:1.3.1")
}

tasks.withType<Javadoc> {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")

    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption("source", "21")
    javadocOptions.encoding = "UTF-8"
}

var bundled = false

val generateBuildInfo = tasks.register("generateBuildInfo") {

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
                public static final String GIT_COMMIT = "${"git rev-parse HEAD".runCommand()}";
                public static final java.time.Instant BUILT_AT = java.time.Instant.ofEpochMilli(${System.currentTimeMillis()}L);
                public static final boolean DEPENDENCIES_BUNDLED=${bundled};
            }
            """.trimIndent()
        )
        println("Generated BuildInfo.java at $buildInfoFile")
    }
}

tasks.register("fatJar") { // all included
    group = "Accessory Build"
    description = "Builds Cytosis ready to ship with all dependencies included in the final jar."
    bundled = true
    dependsOn(fatShadow)
    finalizedBy("copyShadowJarToSecondary", "copyShadowJarToPrimary")
}

tasks.register("thinJar") {
    group = "Accessory Build"
    description = "Builds Cytosis without including any dependencies included in the final jar. <1Mb jar sizes :)"

    bundled = false
    dependsOn(thinShadow)
    finalizedBy("copyJarToSecondary", "copyJarForDocker")
}

val thinShadow = tasks.register<ShadowJar>("thinShadow") {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")

    mergeServiceFiles()
    archiveFileName.set("cytosis.jar")
    archiveClassifier.set("")
    destinationDirectory.set(
        file(
            providers.gradleProperty("server_dir")
                .orElse(layout.buildDirectory.dir("libs").get().toString())
        )
    )
    from(sourceSets.main.get().output)

    configurations = listOf(project.configurations.runtimeClasspath.get())

    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.bootstrap.Bootstrapper"
    }
}

val fatShadow = tasks.register<ShadowJar>("fatShadow") {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")

    mergeServiceFiles()
    archiveFileName.set("cytosis.jar")
    archiveClassifier.set("")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")


    configurations = listOf(
        project.configurations.runtimeClasspath.get(),
        project.configurations.getByName("runtimeDownload"),
        project.configurations.getByName("runtimeDownloadOnly")
    )
    from(sourceSets.main.get().output)

    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.bootstrap.Bootstrapper"
    }
}

tasks.register<Copy>("copyShadowJarToPrimary") {
    dependsOn(fatShadow)

    if (providers.gradleProperty("server_dir").isPresent) {
        from(fatShadow.get().archiveFile)
        into(providers.gradleProperty("server_dir"))
    }
}
tasks.register<Copy>("copyShadowJarToSecondary") {
    dependsOn(fatShadow)

    if (providers.gradleProperty("server_dir2").isPresent) {
        from(fatShadow.get().archiveFile)
        into(providers.gradleProperty("server_dir2"))
    }
}

tasks.jar {
    manifest {
        attributes["Signing-Required"] = "false"
    }
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
}

tasks.register<Copy>("copyJarForDocker") {
    dependsOn(thinShadow)
    from(thinShadow.get().archiveFile)
    into(layout.buildDirectory.dir("libs"))
}
tasks.register<Copy>("copyJarToSecondary") {
    dependsOn(thinShadow)

    if (providers.gradleProperty("server_dir2").isPresent) {
        from(thinShadow.get().archiveFile)
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
            artifact(fatShadow.get().archiveFile)
            artifact(javadocJar)
            artifact(sourcesJar)
        }
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

