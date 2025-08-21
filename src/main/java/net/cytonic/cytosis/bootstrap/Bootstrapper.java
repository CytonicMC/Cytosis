package net.cytonic.cytosis.bootstrap;

import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.path.DependencyPathProvider;
import dev.vankka.dependencydownload.repository.MavenRepository;
import dev.vankka.dependencydownload.resource.DependencyDownloadResource;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.BootstrapLogger;
import net.cytonic.cytosis.utils.BuildInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Bootstrapper {
    public static void main(String[] args) throws IOException {
        if (!BuildInfo.DEPENDENCIES_BUNDLED) {
            long start = System.currentTimeMillis();
            BootstrapLogger.info("Loading dependencies");
            DependencyManager manager = new DependencyManager(DependencyPathProvider.directory(Paths.get("cache")));

            Executor executor = Executors.newFixedThreadPool(3);

            manager.loadResource(DependencyDownloadResource.parse(Objects.requireNonNull(Bootstrapper.class.getResource("/runtimeDownloadOnly.txt"))));
            manager.loadResource(DependencyDownloadResource.parse(Objects.requireNonNull(Bootstrapper.class.getResource("/runtimeDownload.txt"))));
            manager.downloadAll(executor, List.of(
                            new MavenRepository("https://repo1.maven.org/maven2/"),
                            new MavenRepository("https://repo.foxikle.dev/cytonic/"),
                            new MavenRepository("https://jitpack.io/")
                    ))
                    .thenAccept(unused -> {
                        manager.loadAll(executor, new BootstrapClasspathAppender()).join(); // ClasspathAppender is a interface that you need to implement to append a Path to the classpath
                        long end = System.currentTimeMillis();
                        BootstrapLogger.info("Loaded dependencies in " + (end - start) + "ms.");
                        Thread.currentThread().setContextClassLoader(BootstrapClasspathAppender.CLASSLOADER);
                        BootstrapLogger.info("Loading Cytosis Core classes");
                        try {
                            File jarFile = new File(Bootstrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                            URL jarUrl = jarFile.toURI().toURL();
                            BootstrapClasspathAppender.CLASSLOADER.addURL(jarUrl);
                            Class<?> mainClass = BootstrapClasspathAppender.CLASSLOADER.loadClass("net.cytonic.cytosis.Cytosis");
                            BootstrapLogger.info("Starting Cytosis!");
                            try {
                                mainClass.getDeclaredMethod("main", String[].class).invoke(null, (Object) args);
                            } catch (Exception e) {
                                BootstrapLogger.error("Caught exception: ", e);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .exceptionally(throwable -> {
                        BootstrapLogger.error("Failed to load dependency ", throwable);
                        return null;
                    });
        } else {
            Cytosis.main(args);
        }
    }
}
