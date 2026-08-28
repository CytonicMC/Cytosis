package net.cytonic.nativeimage;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/// Stolen from
/// [hollow-cube/mapmaker](https://github.com/hollow-cube/mapmaker/blob/main/tools/native-image-helper/src/main/java/net/hollowcube/nativeimage/HCNativeImageFeature.java)
/// with some modifications, mostly removing their proprietary stuff
///
/// Responsible for doing a bunch of dynamic registration required for native image.
///
/// * Minestom MetadataDef subclasses are registered for runtime lookup.
public class NativeImageFeature implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try (ScanResult scanResult = new ClassGraph()
            .overrideClasspath(access.getApplicationClassPath())
            .enableClassInfo()
            .enableFieldInfo()
            .enableAnnotationInfo()
            .enableMethodInfo()
            .ignoreClassVisibility()
            .ignoreMethodVisibility()
            .ignoreFieldVisibility()
            .acceptPackages("net.cytonic", "net.minestom")
            .scan()) {

            scanResult.getSubclasses(Record.class).forEach(ci -> processRecordClass(access, ci));

            processScan(scanResult.getSubclasses("me.devnatan.inventoryframework.View"), access);
            processScan(scanResult.getClassesImplementing("net.minestom.server.event.Event"), access);
            processScan(scanResult.getClassesImplementing("net.cytonic.protocol.notify.NotifyListener"), access);
            processScan(scanResult.getClassesWithMethodAnnotation("net.cytonic.protocol.utils.NotifyHandler"), access);
            processScan(scanResult.getClassesWithMethodAnnotation("net.cytonic.cytosis.events.api.Listener"), access);
            processScan(scanResult.getClassesWithAnnotation("jakarta.persistence.Embeddable"), access);
            processScan(scanResult.getClassesWithAnnotation("jakarta.persistence.Entity"), access);
            processScan(scanResult.getSubclasses("net.cytonic.protocol.ProtocolObject"), access);
            processScan(scanResult.getSubclasses("net.cytonic.cytosis.commands.utils.CytosisCommand"), access);
            processScan(scanResult.getSubclasses("net.cytonic.protocol.Endpoint"), access);
            processScan(scanResult.getSubclasses("net.cytonic.cytosis.entity.npc.NPC"), access);
            processScan(
                scanResult.getClassesWithAnnotation("net.cytonic.cytosis.bootstrap.annotations.CytosisComponent"),
                access);

            processMinestomMetadataDef(access);
        }

        try (ScanResult scanResult = new ClassGraph()
            .overrideClasspath(access.getApplicationClassPath())
            .enableClassInfo()
            .ignoreClassVisibility()
            .acceptPackages("net.minestom", "net.kyori", "ch.qos.logback", "org.jctools", "it.unimi.dsi.fastutil",
                "com.google.gson", "io.nats.client")
            .scan()) {

            for (var packageInfo : scanResult.getPackageInfo()) {
//                if ("net.minestom.server.network".equals(packageInfo.getName()))
//                    continue;
                if ("net.minestom.server.utils".equals(packageInfo.getName()))
                    continue;
                if ("net.minestom.server.network.packet".equals(packageInfo.getName()))
                    continue;
                IO.println("PI: " + packageInfo.getName());
                RuntimeClassInitialization.initializeAtBuildTime(packageInfo.getName());
            }

            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("net.minestom.server.network.NetworkBufferImpl"));
            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("net.minestom.server.network.NetworkBufferImpl$CompressionHolder"));
            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("net.minestom.server.utils.PacketViewableUtils$ViewableStorage"));
            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("net.minestom.server.utils.ObjectPool"));
            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("net.minestom.server.network.player.PlayerSocketConnection"));
            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("net.minestom.server.network.packet.PacketVanilla"));

            // LoginListener holds a static SecureRandom. GraalVM disallows Random/SecureRandom instances in the
            // image heap entirely, so it must not be initialized at build time by the blanket net.minestom init.
            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("net.minestom.server.listener.preplay.LoginListener"));

            // https://github.com/nats-io/nats.java#integration-with-graalvm
            RuntimeClassInitialization.initializeAtRunTime(access.findClassByName("java.security.SecureRandom"));
            RuntimeClassInitialization.initializeAtRunTime(
                access.findClassByName("io.nats.client.support.RandomUtils"));
            RuntimeClassInitialization.initializeAtRunTime(access.findClassByName("io.nats.client.NUID"));
        }

    }

    private void processRecordClass(BeforeAnalysisAccess access, ClassInfo info) {
        if (info.getName().endsWith("Event")) return;
        var recordClass = access.findClassByName(info.getName());

        RuntimeReflection.register(recordClass);
        for (var ctor : recordClass.getDeclaredConstructors())
            RuntimeReflection.register(ctor);
        for (var comp : recordClass.getRecordComponents())
            RuntimeReflection.register(comp.getAccessor());
    }


    private void processMinestomMetadataDef(BeforeAnalysisAccess access) {
        var metadataDefClass = access.findClassByName("net.minestom.server.entity.MetadataDef");
        RuntimeReflection.registerClassLookup(metadataDefClass.getName());

        // Also add all the subclasses.
        for (Class<?> subclass : metadataDefClass.getDeclaredClasses()) {
            RuntimeReflection.registerClassLookup(subclass.getName());
        }
    }

    private void processScan(ClassInfoList list, final BeforeAnalysisAccess access) {
        list.forEach(classInfo -> {
            var clazz = access.findClassByName(classInfo.getName());
            RuntimeReflection.register(clazz);
            RuntimeReflection.registerClassLookup(classInfo.getName()); // <-- enables Class.forName
            IO.println("Registering: " + classInfo.getName());
            RuntimeReflection.registerAllMethods(clazz);
            for (var method : clazz.getDeclaredMethods())
                RuntimeReflection.register(method);
            for (var ctor : clazz.getDeclaredConstructors())
                RuntimeReflection.register(ctor);
        });
    }

}