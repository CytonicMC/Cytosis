package net.cytonic.cytosis.bootstrap;

import dev.vankka.dependencydownload.classpath.ClasspathAppender;
import net.cytonic.cytosis.logging.BootstrapLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class BootstrapClasspathAppender implements ClasspathAppender {

    public static final BootstrapClassLoader CLASSLOADER = new BootstrapClassLoader();

    @Override
    public void appendFileToClasspath(@NotNull Path path) {
        if (System.getProperty("cytosis.log-bootstrap-appending") != null) {
            BootstrapLogger.info("Appending path to classpath: " + path);
        }

        try {
            CLASSLOADER.addURL(path.toUri().toURL());
        } catch (Exception e) {
            BootstrapLogger.error("Failed to add URL to classpath", e);
            throw new RuntimeException("Failed to add URL to classloader", e);
        }
    }
}
