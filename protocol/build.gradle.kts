import com.github.vlsi.jandex.JandexTask

plugins {
    id("io.freefair.lombok") version "9.5.0"
    `maven-publish`
    `java-library`
    java
    id("com.github.vlsi.jandex") version "3.0.2"
}

group = "net.cytonic"
version = "1.3.4-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.codec)
    implementation(libs.gson)
    implementation(libs.minimessage)
    implementation(libs.adventure.serializer.gson)
    implementation(libs.jandex)
    implementation(libs.jnats)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j2.impl)
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

jandex {
    toolVersion = "3.6.0"
}

tasks.named<JandexTask>("jandexMain") {
    indexFile = file("build/jandex/jandexMain/protocol-jandex.idx")
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