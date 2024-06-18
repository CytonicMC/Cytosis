package net.cytonic.cytosis.plugins;

/**
 * The interface for Cytosis plugins
 */
public interface CytosisPlugin {
    /**
     * Called when the plugin is loaded
     */
    void initialize();

    /**
     * Calledwhen the plugin is unloaded
     */
    void shutdown();
}
