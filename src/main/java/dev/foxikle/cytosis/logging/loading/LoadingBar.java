/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package dev.foxikle.cytosis.logging.loading;

import dev.foxikle.cytosis.logging.Logger;

interface LoadingBar {

    static LoadingBar console(String initialMessage) {
        return new LoggingLoadingBar(initialMessage, System.out::print);
    }

    static LoadingBar logger(String initialMessage, Logger logger) {
        return new LoggingLoadingBar(initialMessage, logger::print);
    }

    LoadingBar subTask(String task);

    StatusUpdater updater();

    String message();
}
