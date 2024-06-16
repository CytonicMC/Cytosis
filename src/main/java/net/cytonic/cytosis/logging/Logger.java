/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package net.cytonic.cytosis.logging;

/**
 * The blueprint for all loggers.
 */
public interface Logger {
    /**
     * Gets the default logger
     *
     * @return the default logger
     */
    static Logger logger() {
        return LoggerImpl.DEFAULT;
    }

    /**
     * Debug log entries contain common debug information.
     * @return the debug logger
     */
    private static Logger debug() {
        return logger().level(Level.DEBUG);
    }

    /**
     * Logs a debug message
     * @param message The message to log
     * @param args The arguments to format the message
     * @return The logger used
     */
    static Logger debug(String message, Object... args) {
        if (args.length == 0) return debug().println(message);
        return debug().printf(message, args).println();
    }

    /**
     * Logs a throwable
     * @param throwable the throwable to log
     * @param args any additional arguments
     * @return the logger used
     */
    static Logger debug(Throwable throwable, Object... args) {
        return debug().throwable(throwable, args);
    }

    /**
     * Setup log entries contain information about the setup of the application.
     * @return the setup logger
     */
    static Logger setup() {
        return logger().level(Level.SETUP);
    }

    /**
     * Logs a setup message
     * @param message The message to log
     * @param args The arguments to format the message
     * @return The logger used
     */
    static Logger setup(String message, Object... args) {
        if (args.length == 0) return setup().println(message);
        return setup().printf(message, args);
    }

    /**
     * Logs a throwable in the setup level
     * @param throwable The throwable to log
     * @param args any additional arguments
     * @return The logger used
     */
    static Logger setup(Throwable throwable, Object... args) {
        return setup().throwable(throwable, args);
    }

    /**
     * Info log entries contain important relevant information.
     * @return the info logger
     */
    private static Logger info() {
        return logger().level(Level.INFO);
    }

    /**
     * Logs a message in the INFO level
     * @param message The message to log
     * @param args The args to format the message
     * @return The logger used
     */
    static Logger info(String message, Object... args) {
        if (args.length == 0) return info().println(message);
        return info().printf(message, args).println();
    }

    /**
     * Logs a throwable in the INFO level (Why would you do this??)
     * @param throwable The throwable to log
     * @param args any additional arguments
     * @return The logger used
     */
    static Logger info(Throwable throwable, Object... args) {
        return info().throwable(throwable, args);
    }

    /**
     * Warn log entries contain technical warnings. Typically, warnings do not prevent the application from continuing.
     * @return the warn logger
     */
    private static Logger warn() {
        return logger().level(Level.WARN);
    }

    /**
     * Logs a warning
     * @param message The message to log
     * @param args The arguments to format the message
     * @return The logger used
     */
    static Logger warn(String message, Object... args) {
        if (args.length == 0) return warn().println(message);
        return warn().printf(message, args);
    }

    /**
     * Logs a throwable in the WARN level
     * @param throwable The throwable to log
     * @param args any additional arguments
     * @return The logger used
     */
    static Logger warn(Throwable throwable, Object... args) {
        return warn().throwable(throwable, args);
    }

    /**
     * Error log entries contain technical errors. Errors WILL stop the application from continuing.
     * @return the error logger
     */
    private static Logger error() {
        return logger().level(Level.ERROR);
    }

    /**
     * Logs an error message in the ERROR level
     * @param message the message to log
     * @param args the arguments used to format the message
     * @return the logger used
     */
    static Logger error(String message, Object... args) {
        if (args.length == 0) return error().println(message);
        return error().printf(message, args);
    }

    /**
     * Logs an error with a throwable
     * @param message the message to log
     * @param ex the throwable to log
     * @return the logger used
     */
    static Logger error(String message, Throwable ex) {
        return error().throwable(message, ex);
    }

    /**
     * Logs an error with a throwable
     * @param throwable the throwable to log
     * @param args the arguments used to format the message
     * @return the logger used
     */
    static Logger error(Throwable throwable, Object... args) {
        return error().throwable(throwable, args);
    }

    /**
     * Set the level of the logger
     *
     * @param level the level
     * @return the logger
     */
    Logger level(Level level);

    /**
     * Gets the current level of the logger
     * @return the level of the logger
     */
    Level level();

    /**
     * Print a message
     * @param message the message
     * @return the logger
     */
    Logger print(String message);

    /**
     * Print a message with a new line at the end
     * @param message the message
     * @return the logger
     */
    default Logger println(String message) {
        return print(message).println();
    }

    /**
     * Print a new line
     * @return the logger
     */
    default Logger println() {
        return print(System.lineSeparator());
    }

    /**
     * Prints a formatted message
     * @param message the message
     * @param args the formatting
     * @return the logger used
     */
    Logger printf(String message, Object... args);

    /**
     * Log a throwable
     * @param throwable the throwable to log
     * @param args any formatting
     * @return the logger used
     */
    Logger throwable(Throwable throwable, Object... args);

    /**
     * Logs a message with a throwable
     * @param message the message
     * @param throwable the throwable
     * @return the logger used
     */
    Logger throwable(String message, Throwable throwable);

    /**
     * Ensures that this logger is ready to print to a blank fresh line.
     *
     * @return the logger
     */
    Logger nextLine();
}