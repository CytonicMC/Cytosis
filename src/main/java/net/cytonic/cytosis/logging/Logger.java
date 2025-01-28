/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package net.cytonic.cytosis.logging;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * The logger interface
 */
public interface Logger {
    /**
     * The logger instance
     */
    ExtendedLogger LOGGER = LogManager.getContext(false).getLogger("Cytosis");

    /**
     * Traces a message in the TRACE level
     * @param message The message to log
     * @param args The arguments to format the message
     */
    static void trace(String message, Object... args) {
        LOGGER.trace(message, args);
    }

    /**
     * Logs a debug message
     * @param message The message to log
     * @param args The arguments to format the message
     */
    static void debug(String message, Object... args) {
        // shut, that causes it to call a differnt method
        LOGGER.atLevel(LogLevel.CYTOSIS_DEBUG).log("\u001B[0;95m" + message, args);
    }


    /**
     * Logs a message in the INFO level
     * @param message The message to log
     * @param args The args to format the message
     */
    static void info(String message, Object... args) {
        LOGGER.info(message, args);
    }

    /**
     * Logs a warning
     * @param message The message to log
     * @param args The arguments to format the message
     */
    static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }

    /**
     * Logs an error message in the ERROR level
     * @param message the message to log
     * @param args the arguments used to format the message
     */
    static void error(String message, Object... args) {
        Component component = Msg.mm("<red><b>Error Logged on server '" + Cytosis.SERVER_ID + "'</b></red><newline><gray> Message: " + message);
        Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.SERVER_ERROR, SnoopUtils.toSnoop(component));
        LOGGER.error(message, args);
    }

    /**
     * Logs an error with a throwable
     * @param message the message to log
     * @param ex the throwable to log
     */
    static void error(String message, Throwable ex) {
        Component component = Msg.mm("<red><b>Error Logged on server '" + Cytosis.SERVER_ID + "'</b></red><newline><gray> Message: " + message + "</gray><newline><red><b>Throwable:<b></red><gray> " + ex.getMessage());
        Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.SERVER_ERROR, SnoopUtils.toSnoop(component));
        LOGGER.error(message, ex);
    }
}