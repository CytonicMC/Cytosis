import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo

plugins {
    `maven-publish`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
    id("dev.vankka.dependencydownload.plugin") version "2.0.0"
    id("io.freefair.lombok") version "9.1.0"
    id("net.kyori.blossom") version "2.2.0"
    id("net.kyori.indra.git") version "4.0.0"
    id("checkstyle")
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

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

dependencies {
    download(libs.minestom)
    download(libs.gson)
    download(libs.jnats)
    download(libs.okhttp)
    download(libs.polar)
    download(libs.jedis)
    download(libs.guava)
    download(libs.minestompvp) {
        exclude(group = "net.minestom", module = "minestom")
    }
    download(libs.invui)
    download(libs.anvilInput)
    download(libs.configurate)
    download(libs.classgraph)
    download(libs.jnats)
    download(libs.jooq)
    download(libs.minimessage)
    download(libs.fastutil)
    download(libs.hikaricp)
    download(libs.reflections)
    download(libs.bundles.log4j)
    download(libs.bundles.otel)
    download(libs.mysql)

    implementation(libs.dependencydownload)

    //shuts Gradle up about how lombok goes above and beyond (jakarta bind XML)
    compileOnly(libs.lombokwarningfix)
}


fun DependencyHandler.download(dependencyNotation: Any) {
    val resolved = when (dependencyNotation) {
        is Provider<*> -> dependencyNotation.get()
        else -> dependencyNotation
    }

    if (resolved is Iterable<*>) {
        resolved.forEach { dep ->
            add("api", dep!!)
            add("runtimeDownloadOnly", dep)
        }
    } else {
        add("api", resolved)
        add("runtimeDownloadOnly", resolved)
    }
}

fun DependencyHandler.download(
    dependencyNotation: Any,
    dependencyConfiguration: Action<ExternalModuleDependency>
) {
    val resolved = when (dependencyNotation) {
        is Provider<*> -> dependencyNotation.get()
        else -> dependencyNotation
    }

    addDependencyTo(
        this, "api", resolved, dependencyConfiguration
    )
    addDependencyTo(
        this, "runtimeDownloadOnly", resolved, dependencyConfiguration
    )
}

tasks.withType<Javadoc> {
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")

    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption("source", "25")
    javadocOptions.encoding = "UTF-8"
}

val bundled = gradle.startParameter.taskNames.any { it.contains("fatJar") || it.contains("fatShadow") }

sourceSets {
    main {
        blossom {
            javaSources {
                property("buildVersion", project.version.toString())
                property("gitCommit", indraGit.commit().get().name())
                properties.put("builtAt", System.currentTimeMillis())
                properties.put("dependenciesBundled", bundled)
            }
        }
    }
}

tasks.register("fatJar") { // all included
    group = "Accessory Build"
    description = "Builds Cytosis ready to ship with all dependencies included in the final jar."
    dependsOn(fatShadow)
    finalizedBy("copyShadowJarToSecondary", "copyShadowJarToPrimary")
}

tasks.register("thinJar") {
    group = "Accessory Build"
    description = "Builds Cytosis without including any dependencies included in the final jar. <1Mb jar sizes :)"

    dependsOn(thinShadow)
    finalizedBy("copyJarToSecondary", "copyJarForDocker")
}

val thinShadow = tasks.register<ShadowJar>("thinShadow") {
//    dependsOn("check")
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

    configurations = listOf(dependencyDownloadOnly)

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

// Create a custom configuration for only the dependency download plugin
val dependencyDownloadOnly: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    dependencyDownloadOnly(libs.dependencydownload)
}

val apiArtifacts: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(configurations.getByName("api"))
}

val apiJars = apiArtifacts
    .resolvedConfiguration
    .resolvedArtifacts
    .map { it.file }

val fatShadow = tasks.register<ShadowJar>("fatShadow") {
//    dependsOn("check")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownloadOnly")
    dependsOn("generateRuntimeDownloadResourceForRuntimeDownload")

    mergeServiceFiles()
    archiveFileName.set("cytosis.jar")
    archiveClassifier.set("all")
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

tasks.shadowJar {
    enabled = false
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
    toolVersion = "12.3.1"
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