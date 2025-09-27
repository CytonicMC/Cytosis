import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `maven-publish`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
    id("com.github.harbby.gradle.serviceloader") version ("1.1.9")
    id("dev.vankka.dependencydownload.plugin") version "2.0.0"
    id("io.freefair.lombok") version "8.14.2"
    id("checkstyle")
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.foxikle.dev/cytonic")
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
        content { // This filtering is optional, but recommended
            includeModule("net.minestom", "minestom")
            includeModule("net.minestom", "testing")
        }
    }
}

dependencies {
    api(libs.minestom)
    api(libs.gson)
    api(libs.okhttp)
    api(libs.polar)
    api(libs.jedis)
    api(libs.guava)
    api(libs.minestompvp) {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }
    api(libs.stomui) {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }
    api(libs.configurate)
    api(libs.classgraph)
    api(libs.jnats)
    api(libs.jooq)
    api(libs.mixin)


    // gets gradle to shut up about how lombok goes above and beyond (jakarta bind xml)
    compileOnly(libs.lombokwarningfix)


    runtimeDownload(libs.minimessage)
    runtimeDownload(libs.mysql)
    runtimeDownload(libs.reflections)
    runtimeDownload(libs.bundles.log4j)
    runtimeDownload(libs.bundles.otel)

    // the compileonlyapis need to be downloaded at runtime, too.
    runtimeDownloadOnly(libs.minestom)
    runtimeDownloadOnly(libs.jnats)
    runtimeDownloadOnly(libs.jooq)
    runtimeDownloadOnly(libs.gson)
    runtimeDownloadOnly(libs.okhttp)
    runtimeDownloadOnly(libs.polar)
    runtimeDownloadOnly(libs.jedis)
    runtimeDownloadOnly(libs.guava)
    runtimeDownloadOnly(libs.stomui) {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }
    runtimeDownloadOnly(libs.configurate)
    runtimeDownloadOnly(libs.classgraph)
    runtimeDownloadOnly(libs.minestompvp) {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }
    runtimeDownloadOnly(libs.mixin)

    // Dependency loading
    implementation(libs.dependencydownload)
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
            
            /**
            * Holds information on the current build of Cytosis. This code is auto-generated at build.
            */
            public class BuildInfo {
                public static final String BUILD_VERSION = "${project.version}";
                public static final String GIT_COMMIT = "${"git rev-parse HEAD".runCommand()}";
                public static final java.time.Instant BUILT_AT = java.time.Instant.ofEpochMilli(${System.currentTimeMillis()}L);
                public static final boolean DEPENDENCIES_BUNDLED = ${bundled};
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
    dependsOn("check")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

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

    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.bootstrap.Bootstrapper"
    }
}

val apiArtifacts by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(configurations.getByName("api"))
}

val apiJars = apiArtifacts
    .resolvedConfiguration
    .resolvedArtifacts
    .map { it.file }

thinShadow.configure {
    configurations = listOf(project.configurations.runtimeClasspath.get())

    doFirst {
        apiJars.forEach { jar ->
            exclude { it.file == jar }
        }
    }
}

val fatShadow = tasks.register<ShadowJar>("fatShadow") {
    dependsOn("check")
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

tasks.shadowJar {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
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
            from(components["java"])
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

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<ShadowJar> {
    // prevents issues with security exceptions
    exclude("META-INF/**/*.SF")
    exclude("META-INF/**/*.DSA")
    exclude("META-INF/**/*.RSA")
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

// Checkstyle configuration
checkstyle {
    toolVersion = "10.26.1"
    configFile = file("${rootDir}/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(file("$projectDir/build/reports/checkstyle/${name}.html"))
    }

    // Always generate reports, even on failure
    isIgnoreFailures = true
}

// Configure checkstyle tasks
tasks.named<Checkstyle>("checkstyleMain") {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Checkstyle>("checkstyleTest") {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Make check task depend on checkstyle
tasks.named("check") {
    dependsOn("checkstyleMain", "checkstyleTest")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
}

