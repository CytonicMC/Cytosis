import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `maven-publish`
    `java-library`
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.harbby.gradle.serviceloader") version ("1.1.8")
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.foxikle.dev/cytonic")
}

dependencies {
    api("net.cytonic:Commons:1.6.1")
    api("net.cytonic:CytosisPluginProcessor:1.0")
    api("net.minestom:minestom-snapshots:916424e995")
    api("com.google.code.gson:gson:2.11.0") // serializing
    api("com.squareup.okhttp3:okhttp:4.12.0") // http api requests
    implementation("net.kyori:adventure-text-minimessage:4.17.0")// better components
    implementation("com.mysql:mysql-connector-j:9.1.0") //mysql connector
    compileOnly("org.projectlombok:lombok:1.18.34") // lombok
    annotationProcessor("org.projectlombok:lombok:1.18.34") // lombok
    implementation("org.tomlj:tomlj:1.1.1") // Config lang
    api("com.rabbitmq:amqp-client:5.22.0") // Message broker
    api("dev.hollowcube:polar:1.11.3") // Polar
    api("redis.clients:jedis:5.2.0") // redis client
    api("com.google.guava:guava:33.3.1-jre")
    implementation("org.reflections:reflections:0.10.2") // reflection utils
    implementation("io.kubernetes:client-java:21.0.2")
    implementation("org.slf4j:slf4j-api:2.0.16")  // SLF4J API
    implementation("org.apache.logging.log4j:log4j-core:2.24.1")  // Log4j core
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")
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
tasks.withType<Javadoc> {
    val javadocOptions = options as CoreJavadocOptions

    javadocOptions.addStringOption("source", "21")
    javadocOptions.addBooleanOption("-enable-preview", true)
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
        archiveClassifier.set("")
        destinationDirectory.set(
            file(
                providers.gradleProperty("server_dir").orElse(destinationDirectory.get().toString())
            )
        )
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