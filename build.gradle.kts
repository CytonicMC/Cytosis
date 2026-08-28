import com.github.vlsi.jandex.JandexTask
import io.ebean.annotation.Platform.POSTGRES
import org.jboss.jandex.IndexWriter
import org.jboss.jandex.Indexer
import java.util.jar.JarFile

plugins {
    `maven-publish`
    `java-library`
    id("java")
    id("io.freefair.lombok") version "9.5.0"
    alias(libs.plugins.blossom)
    alias(libs.plugins.indragit)
    id("checkstyle")
    id("io.ebean") version "19.3.0"
    id("net.cytonic.migration-generator") version "1.0-SNAPSHOT"
    id("dev.minestom-united.minestom-events") version "0.0.2"
    id("com.github.vlsi.jandex") version "3.0.2"
    id("org.graalvm.buildtools.native") version "1.1.9"
}

group = "net.cytonic"

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
    api(project(":protocol"))
    implementation(project(":nativeimage"))
    api(libs.entrypoint)
    api(libs.codecutils)

    api(libs.jnats) {
        exclude(group = "org.bouncycastle", module = "bcprov-lts8on")
    }
    api(libs.okhttp)
    api(libs.polar)
    api(libs.jedis)
    api(libs.mongo)
    api(libs.guava)
    api(libs.minestompvp) {
        exclude(group = "net.minestom", module = "minestom")
    }
    api(libs.invui)
    api(libs.anvilInput)
    api(libs.fastutil)
    api(libs.hikaricp)
    api(libs.reflections)
    api(libs.logback)
    api(libs.bundles.otel)
    api(libs.postgresql)
    api(libs.joml)
    api(libs.ebean)
    api(libs.ebean.ddl)
    api(libs.ebean.migrations)
    api(libs.jandex)
    api(libs.minio) {
        exclude(group = "org.bouncycastle", module = "bcprov-lts8on")
    }
    implementation(libs.bouncycastle)
    api(libs.minimessage)
    api(libs.minestomevents)
    annotationProcessor(libs.ebean.query)
    //shuts Gradle up about how lombok goes above and beyond (jakarta bind XML)
    compileOnly(libs.lombokwarningfix)
}

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("io.smallrye:jandex:3.6.0")
    }
}

migration {
    id = "cytosis"
    platform = POSTGRES
    databases = listOf("global", "environment")
}

minestomEvents {
    outputPackage = "net.cytonic.cytosis.utils"
}

val buildIndex = tasks.register("indexMinestomEvents") {
    group = "build"
    description = "Indexes net.minestom.server.event from the Minestom dependency jar."

    // Depend on configuration resolution so the jar is present
    dependsOn("ebeanEnhance")

    val outputFile = layout.buildDirectory.file("resources/main/META-INF/minestom-jandex.idx")
    outputs.file(outputFile)

    doLast {
        val indexer = Indexer()

        val minestomJar = configurations["runtimeClasspath"]
            .resolvedConfiguration
            .resolvedArtifacts
            .first { it.name == "minestom" }
            .file

        JarFile(minestomJar).use { jar ->
            jar.entries().asSequence()
                .filter { entry ->
                    entry.name.startsWith("net/minestom/server/event/")
                            && entry.name.endsWith(".class")
                }
                .forEach { entry ->
                    jar.getInputStream(entry).use(indexer::index)
                }
        }
        val idx = indexer.complete()

        val out = outputFile.get().asFile
        out.parentFile.mkdirs()
        out.outputStream().use { IndexWriter(it).write(idx) }
    }
}

tasks.withType<Javadoc> {
    dependsOn(buildIndex)

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

tasks {
    jar {
        dependsOn("indexMinestomEvents")
    }
}


jandex {
    toolVersion = "3.6.0"
}

tasks.named<JandexTask>("jandexMain") {
    indexFile = file("build/jandex/jandexMain/cytosis-jandex.idx")
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
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.matching("GraalVM")
        nativeImageCapable = true
    }
}

// Checkstyle configuration
checkstyle {
    toolVersion = "13.8.0"
    configFile = file("${rootDir}/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

tasks.withType<Checkstyle>().configureEach {
    dependsOn(buildIndex)

    exclude("**/Events.java")

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(file("$projectDir/build/reports/checkstyle/${name}.html"))
    }

    // Always generate reports, even on failure
    isIgnoreFailures = true
}

afterEvaluate {
    tasks.findByName("generateMigrationEnvironment")?.apply {
        dependsOn(buildIndex)
    }
}

// Configure checkstyle tasks
tasks.named<Checkstyle>("checkstyleMain") {
    dependsOn("processJandexIndex")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Checkstyle>("checkstyleTest") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Make check task depend on checkstyle
tasks.named("check") {
    dependsOn("checkstyleMain", "checkstyleTest")
}