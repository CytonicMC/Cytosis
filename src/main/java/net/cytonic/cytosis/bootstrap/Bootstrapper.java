package net.cytonic.cytosis.bootstrap;

import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.repository.StandardRepository;
import dev.vankka.dependencydownload.resource.DependencyDownloadResource;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.BootstrapLogger;
import net.cytonic.cytosis.utils.BuildInfo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Bootstrapper {
    public static void main(String[] args) throws URISyntaxException, IOException {
        BootstrapLogger.info("Starting boostrapper");
        BootstrapLogger.info("Classpath: " + System.getProperty("java.class.path"));

        if (!BuildInfo.DEPENDENCIES_BUNDLED) {
            long start = System.currentTimeMillis();
            BootstrapLogger.info("Loading dependencies");
            DependencyManager manager = new DependencyManager(Paths.get("cache"));

            Executor executor = Executors.newFixedThreadPool(3);

            manager.loadFromResource(new DependencyDownloadResource(Bootstrapper.class.getResource("/runtimeDownloadOnly.txt").toURI().toURL()));
            manager.loadFromResource(new DependencyDownloadResource(Bootstrapper.class.getResource("/runtimeDownload.txt").toURI().toURL()));
            manager.downloadAll(executor, List.of(
                            new StandardRepository("https://repo1.maven.org/maven2/"),
                            new StandardRepository("https://repo.foxikle.dev/cytonic/"),
                            new StandardRepository("https://jitpack.io/")
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
            BootstrapLogger.info("Skipping bootstraping of dependencies. Starting Cytosis!");
            // start cytosis
            Cytosis.main(args);
        }
    }
}
