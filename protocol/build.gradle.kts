plugins {
    id("io.freefair.lombok") version "9.2.0"
    `maven-publish`
    `java-library`
    java
    id("org.kordamp.gradle.jandex") version "2.3.0"
}

group = "net.cytonic"
version = "1.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
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

tasks {
    javadoc {
        dependsOn("jandex")
        dependsOn(renameJandex)
    }
}

val renameJandex by tasks.registering(Copy::class) {
    dependsOn(tasks.named("jandex"))

    from(layout.buildDirectory.file("resources/main/META-INF/jandex.idx"))
    into(layout.buildDirectory.dir("resources/main/META-INF"))

    rename("jandex.idx", "protocol-jandex.idx")
}

tasks.named("jar") {
    dependsOn(renameJandex)
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