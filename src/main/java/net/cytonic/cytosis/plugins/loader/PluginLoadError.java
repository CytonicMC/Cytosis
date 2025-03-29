package net.cytonic.cytosis.plugins.loader;

/**
 * A custom exception for plugin loading
 */
public class PluginLoadError extends Error {

    public PluginLoadError(String message) {
        super(message);
    }

    public PluginLoadError(String message, Throwable cause) {
        super(message, cause);
    }

    public static PluginLoadError noPluginAnnotation(Class<?> mainClass) {
        return new PluginLoadError("Main class " + mainClass.getName() + " is not annotated with @Plugin");
    }

    public static PluginLoadError noPluginInheritance(Class<?> mainClass) {
        return new PluginLoadError("Main class " + mainClass.getName() + " does not implement CytosisPlugin");
    }

    public static PluginLoadError noMainClass(String jarName) {
        return new PluginLoadError("Plugin " + jarName + " does not have a main class");
    }
}
