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
    implementation("com.github.Minestom", "Minestom", "85942b6b27") // minestom itself
    implementation("com.google.code.gson:gson:2.11.0") // serializing
    implementation("org.slf4j:slf4j-api:2.0.13") // logging
    implementation("net.kyori:adventure-text-minimessage:4.17.0")// better components
    implementation("mysql:mysql-connector-java:8.0.33") //mysql connector
    compileOnly("org.projectlombok:lombok:1.18.32") // lombok
    annotationProcessor("org.projectlombok:lombok:1.18.32") // lombok
    implementation("org.tomlj:tomlj:1.1.1") // Config lang
    implementation("com.rabbitmq:amqp-client:5.21.0") // Message broker
    implementation("dev.hollowcube:polar:1.9.4") // Polar
    implementation("com.google.guava:guava:33.2.0-jre") // a lot of things, but mostly caching
    implementation("redis.clients:jedis:5.1.3") // redis client
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
        //destinationDirectory.set(file(providers.gradleProperty("server_dir").get()))
    }
}