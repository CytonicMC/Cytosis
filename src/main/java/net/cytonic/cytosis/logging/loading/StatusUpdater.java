/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package net.cytonic.cytosis.logging.loading;

/**
 * A statusbar updater
 */
public interface StatusUpdater {
    /**
     * Updates the progress bar without changing the text message.
     *
     * @param progress the progress, between 0 and 1
     */
    void progress(double progress);

    /**
     * Updates the text message without changing the progress bar.
     *
     * @param message the new message
     */
    void message(String message);
}