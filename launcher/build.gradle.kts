plugins {
    id("java")
    application
    id("com.gradleup.shadow") version "9.6.0"
    id("io.freefair.lombok") version "9.5.0"
    id("dev.minestom-united.minestom-events") version "0.0.2"
    id("org.graalvm.buildtools.native") version "1.1.9"
    alias(libs.plugins.blossom)
    alias(libs.plugins.indragit)
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.foxikle.dev/cytonic")
}

dependencies {
    implementation(project(":"))
}

minestomEvents {
    outputPackage = "net.cytonic.cytosis.launcher.utils"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.matching("GraalVM")
        nativeImageCapable = true
    }
}

tasks.named<JavaExec>("run") {
    workingDir = file("run")
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
    application {
        mainClass.set("net.cytonic.cytosis.launcher.CytosisMain")
    }
    shadowJar {
        archiveFileName.set("cytosis.jar")
        archiveClassifier.set("")
        mergeServiceFiles()
    }
}

// stolen from HC
graalvmNative {
    binaries {
        named("main") {
            buildArgs(
                listOf(
                    "--enable-native-access=ALL-UNNAMED", "--enable-monitoring=jfr",
                    "--features=net.cytonic.nativeimage.NativeImageFeature",
//                    "--gc=G1",

//                    "--future-defaults=all",
                    "-H:+UseCompressedReferences", "-R:MaxHeapSize=200m",
                    "--static-nolibc", "--no-fallback",
                    "--emit build-report",

                    // TODO: https enabled because we fetch skins from the session service. Should proxy (with cache)
                    //  this on the servers, or just store skins ourselves on player data
                    "--enable-url-protocols=http,https",

                    "--initialize-at-build-time=net.cytonic.cytosis.StaticInitializers",

                    "--report-unsupported-elements-at-runtime",
                    "--initialize-at-build-time=it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap",
                    $$"--initialize-at-build-time=it.unimi.dsi.fastutil.ints.Int2ObjectMaps$EmptyMap",
                    "--initialize-at-build-time=ch.qos.logback.classic.spi.LogbackServiceProvider",
                )
            )
        }
    }
}
