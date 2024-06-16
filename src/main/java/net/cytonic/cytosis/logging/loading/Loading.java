/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package net.cytonic.cytosis.logging.loading;

import net.cytonic.cytosis.logging.Level;

/**
 * A loading bar. Currently unused.
 */
public interface Loading {

    /**
     * Start the loading animation
     *
     * @param name The name
     */
    static void start(String name) {
        LoadingImpl.CURRENT.waitTask(name);
    }

    /**
     * Get the current status updater
     * @return the status updater
     */
    static StatusUpdater updater() {
        return LoadingImpl.CURRENT.getUpdater();
    }

    /**
     * Finish loading
     */
    static void finish() {
        LoadingImpl.CURRENT.finishTask();
    }

    /**
     * Set the current level
     * @param level the new level
     */
    static void level(Level level) {
        LoadingImpl.CURRENT.level = level;
    }
}