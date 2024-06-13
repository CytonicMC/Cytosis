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

//serviceLoader.serviceInterfaces.add("net.minestom.vanilla.VanillaReimplementation\$Feature")
serviceLoader.serviceInterfaces.add("org.slf4j.spi.SLF4JServiceProvider")

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:b3aa996e1d")
    implementation("com.google.code.gson:gson:2.11.0") // serializing
    implementation("org.slf4j:slf4j-api:2.0.13") // logging
    implementation("net.kyori:adventure-text-minimessage:4.17.0")// better components
    implementation("mysql:mysql-connector-j:8.4.0") //mysql connector
    compileOnly("org.projectlombok:lombok:1.18.32") // lombok
    annotationProcessor("org.projectlombok:lombok:1.18.32") // lombok
    implementation("org.tomlj:tomlj:1.1.1") // Config lang
    implementation("com.rabbitmq:amqp-client:5.21.0") // Message broker
    implementation("dev.hollowcube:polar:1.9.5") // Polar
    implementation("com.google.guava:guava:33.2.1-jre") // a lot of things, but mostly caching
    implementation("redis.clients:jedis:5.1.3") // redis client
    implementation("org.reflections:reflections:0.10.2") // reflection utils
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
        destinationDirectory.set(file(providers.gradleProperty("server_dir").orElse("/home/runner/work/Cytosis/build")))
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
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic") {
                    // Use providers to get the properties or fallback to environment variables
                    var u = System.getenv("REPO_USERNAME")
                    var p = System.getenv("REPO_PASSWORD")

                    if (u == null || u.isEmpty()) {
                        u = "no-value-provided"
                    }
                    if (p == null || p.isEmpty()) {
                        p = "no-value-provided"
                    }

                    println("pass: $p | user: $u")


                    val user = providers.gradleProperty("username").orElse(u).get()
                    val pass = providers.gradleProperty("password").orElse(p).get()

                    println("pass: ${pass.length} | user: ${user.length}")
                    credentials {
                        username = user
                        password = pass
                    }
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