/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package dev.foxikle.cytosis.logging.loading;

import dev.foxikle.cytosis.logging.Level;

public interface Loading {

    static void start(String name) {
        LoadingImpl.CURRENT.waitTask(name);
    }

    static StatusUpdater updater() {
        return LoadingImpl.CURRENT.getUpdater();
    }

    static void finish() {
        LoadingImpl.CURRENT.finishTask();
    }

    static void level(Level level) {
        LoadingImpl.CURRENT.level = level;
    }
}
