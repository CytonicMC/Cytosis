package net.cytonic.cytosis.bootstrap;

import java.nio.file.Path;

import dev.vankka.dependencydownload.classpath.ClasspathAppender;
import net.cytonic.cytosis.bootstrap.mixins.CytosisRootClassLoader;
import net.cytonic.cytosis.logging.BootstrapLogger;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.logging.BootstrapLogger;

/**
 * The {@link Bootstrapper}'s implementation of DependencyDownload's {@link ClasspathAppender}. It handles adding URLs
 * to the {@link BootstrapClassLoader}, logging errors and optionally each path appended to the classpath, via the
 * "cytosis.log-bootstrap-appending" system property.
 */
public class BootstrapClasspathAppender implements ClasspathAppender {

    public static final CytosisRootClassLoader CLASSLOADER = CytosisRootClassLoader.getInstance();

    /**
     * {@inheritDoc}
     *
     * @param path the path
     */
    @Override
    public void appendFileToClasspath(@NotNull final Path path) {
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
