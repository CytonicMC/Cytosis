import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo

plugins {
    `maven-publish`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
    id("dev.vankka.dependencydownload.plugin") version "2.0.0"
    id("io.freefair.lombok") version "9.2.0"
    id("net.kyori.blossom") version "2.2.0"
    id("net.kyori.indra.git") version "4.0.0"
    id("checkstyle")
}

group = "net.cytonic"
version = property("version").toString()

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.foxikle.dev/cytonic")
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
        content {
            includeModule("net.minestom", "minestom")
        }
    }
}

val alwaysShadow: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val downloadOrShadow: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    // Always shadowed (in both thin and fat jars) - the essentials
    alwaysShade(libs.dependencydownload)
    alwaysShade(project(":protocol"))

    // Downloaded at runtime for thinJar, shadowed in fatJar
    downloadOrShade(libs.minestom)
    downloadOrShade(libs.gson)
    downloadOrShade(libs.jnats)
    downloadOrShade(libs.okhttp)
    downloadOrShade(libs.polar)
    downloadOrShade(libs.jedis)
    downloadOrShade(libs.guava)
    downloadOrShade(libs.minestompvp) {
        exclude(group = "net.minestom", module = "minestom")
    }
    downloadOrShade(libs.invui)
    downloadOrShade(libs.anvilInput)
    downloadOrShade(libs.configurate)
    downloadOrShade(libs.classgraph)
    downloadOrShade(libs.jooq)
    downloadOrShade(libs.minimessage)
    downloadOrShade(libs.fastutil)
    downloadOrShade(libs.hikaricp)
    downloadOrShade(libs.reflections)
    downloadOrShade(libs.bundles.log4j)
    downloadOrShade(libs.bundles.otel)
    downloadOrShade(libs.mysql)
    downloadOrShade(libs.joml)

    //shuts Gradle up about how lombok goes above and beyond (jakarta bind XML)
    compileOnly(libs.lombokwarningfix)
}

// alwaysShade: Always shadowed in both thin and fat jars
fun DependencyHandler.alwaysShade(dependencyNotation: Any) {
    val resolved = when (dependencyNotation) {
        is Provider<*> -> dependencyNotation.get()
        else -> dependencyNotation
    }

    if (resolved is Iterable<*>) {
        resolved.forEach { dep ->
            add("api", dep!!)
            add("alwaysShadow", dep)
        }
    } else {
        add("api", resolved)
        add("alwaysShadow", resolved)
    }
}

fun DependencyHandler.alwaysShade(
    dependencyNotation: Any,
    dependencyConfiguration: Action<ExternalModuleDependency>
) {
    val resolved = when (dependencyNotation) {
        is Provider<*> -> dependencyNotation.get()
        else -> dependencyNotation
    }

    addDependencyTo(this, "api", resolved, dependencyConfiguration)
    addDependencyTo(this, "alwaysShadow", resolved, dependencyConfiguration)
}

fun DependencyHandler.downloadOrShade(dependencyNotation: Any) {
    val resolved = when (dependencyNotation) {
        is Provider<*> -> dependencyNotation.get()
        else -> dependencyNotation
    }

    if (resolved is Iterable<*>) {
        resolved.forEach { dep ->
            add("api", dep!!)
            add("runtimeDownloadOnly", dep)
            add("downloadOrShadow", dep)
        }
    } else {
        add("api", resolved)
        add("runtimeDownloadOnly", resolved)
        add("downloadOrShadow", resolved)
    }
}

fun DependencyHandler.downloadOrShade(
    dependencyNotation: Any,
    dependencyConfiguration: Action<ExternalModuleDependency>
) {
    val resolved = when (dependencyNotation) {
        is Provider<*> -> dependencyNotation.get()
        else -> dependencyNotation
    }

    addDependencyTo(this, "api", resolved, dependencyConfiguration)
    addDependencyTo(this, "runtimeDownloadOnly", resolved, dependencyConfiguration)
    addDependencyTo(this, "downloadOrShadow", resolved, dependencyConfiguration)
}

tasks.withType<Javadoc> {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")

    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption("source", "25")
    javadocOptions.encoding = "UTF-8"
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

gradle.taskGraph.whenReady {
    val bundled = hasTask(":fatShadow") || hasTask(":fatJar")
    sourceSets.main.get().blossom.javaSources {
        properties.put("dependenciesBundled", bundled)
    }
}

tasks.register("fatJar") {
    group = "Accessory Build"
    description = "Builds Cytosis ready to ship with all dependencies included in the final jar."
    dependsOn(fatShadow)
    finalizedBy("copyFatToPrimary", "copyFatToSecondary", "copyJarForDocker", "copyFatToLibs")
}

tasks.register("thinJar") {
    group = "Accessory Build"
    description = "Builds Cytosis with only essential dependencies. Downloads the rest at runtime."
    dependsOn(thinShadow)
    finalizedBy("copyThinToPrimary", "copyThinToSecondary", "copyThinToLibs")
}

val thinShadow = tasks.register<ShadowJar>("thinShadow") {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

    mergeServiceFiles()
    archiveClassifier.set("")
    destinationDirectory.set(layout.buildDirectory.dir("temp"))
    from(sourceSets.main.get().output)

    configurations = listOf(alwaysShadow)

    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.bootstrap.Bootstrapper"
    }

    archiveBaseName.set(project.name)
}

tasks.jar {
    enabled = false
}

artifacts {
    archives(thinShadow)
}

configurations {
    apiElements {
        outgoing.artifacts.clear()
        outgoing.artifact(thinShadow)
    }
    runtimeElements {
        outgoing.artifacts.clear()
        outgoing.artifact(thinShadow)
    }
}

val fatShadow = tasks.register<ShadowJar>("fatShadow") {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")

    mergeServiceFiles()
    archiveClassifier.set("all")
    destinationDirectory.set(layout.buildDirectory.dir("temp"))

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

    from(sourceSets.main.get().output)

    configurations = listOf(
        alwaysShadow,
        downloadOrShadow
    )

    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.bootstrap.Bootstrapper"
    }
}

fun registerServerCopy(taskName: String, sourceTask: TaskProvider<ShadowJar>, propertyName: String) {
    tasks.register<Copy>(taskName) {
        dependsOn(sourceTask)

        val destProp = providers.gradleProperty(propertyName)
        onlyIf { destProp.isPresent }

        from(sourceTask.get().archiveFile)
        if (destProp.isPresent) {
            into(destProp)
        }
        rename { "cytosis.jar" }
    }
}

registerServerCopy("copyFatToPrimary", fatShadow, "server_dir")
registerServerCopy("copyFatToSecondary", fatShadow, "server_dir2")

registerServerCopy("copyThinToPrimary", thinShadow, "server_dir")
registerServerCopy("copyThinToSecondary", thinShadow, "server_dir2")

tasks.register<Copy>("copyJarForDocker") {
    dependsOn(fatShadow)
    from(fatShadow.get().archiveFile)
    into(layout.buildDirectory.dir("docker"))
    rename { "cytosis.jar" }
}

tasks.register<Copy>("copyFatToLibs") {
    dependsOn(fatShadow)
    from(fatShadow.get().archiveFile)
    into(layout.buildDirectory.dir("libs"))
    rename { "cytosis.jar" }
}

tasks.register<Copy>("copyThinToLibs") {
    dependsOn(thinShadow)
    from(thinShadow.get().archiveFile)
    into(layout.buildDirectory.dir("libs"))
    rename { "cytosis.jar" }
}

tasks.shadowJar {
    enabled = false
}

tasks.publish {
    dependsOn(fatShadow)
}

tasks.withType<GenerateModuleMetadata> {
    dependsOn(fatShadow)
}

publishing {
    repositories {
        maven {
            name = "FoxikleCytonicRepository"
            url = uri("https://repo.foxikle.dev/cytonic")
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

            artifact(fatShadow) {
                classifier = "all"
            }
        }
    }
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

    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

// Checkstyle configuration
checkstyle {
    toolVersion = "13.0.0"
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