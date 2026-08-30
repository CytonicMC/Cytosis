package net.cytonic.cytosis.logging;

/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.Snoops;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.metrics.CytosisOpenTelemetry;
import net.cytonic.cytosis.utils.Msg;

/**
 * The logger interface
 */
public interface Logger {

    /**
     * The logger instance
     */
    org.slf4j.Logger LOGGER = LoggerFactory.getLogger("Cytosis");

    /**
     * Logs a debug message
     *
     * @param message The message to log
     * @param args    The arguments to format the message
     */
    static void debug(String message, Object... args) {
        // shut, that causes it to call a differnt method
        LOGGER.atLevel(Level.DEBUG).log("\u001B[0;95m" + message.formatted(args));
        emit(Severity.DEBUG, message.formatted(args), null);
    }

    /**
     * Logs a message in the INFO level
     *
     * @param message The message to log
     * @param args    The args to format the message
     */
    static void info(String message, Object... args) {
        LOGGER.info(message.formatted(args));
        emit(Severity.INFO, message.formatted(args), null);
    }

    /**
     * Logs a warning
     *
     * @param message The message to log
     * @param args    The arguments to format the message
     */
    static void warn(String message, Object... args) {
        LOGGER.warn(message.formatted(args));
        emit(Severity.WARN, message.formatted(args), null);
    }

    /**
     * Logs an error message in the ERROR level
     *
     * @param message the message to log
     * @param args    the arguments used to format the message
     */
    static void error(String message, Object... args) {
        LOGGER.error(message.formatted(args));
        emitSnoop(message.formatted(args));
        emit(Severity.ERROR, message.formatted(args), null);
    }

    /**
     * Logs an error with a throwable
     *
     * @param message the message to log
     * @param ex      the throwable to log
     */
    static void error(String message, Throwable ex) {
        LOGGER.error(message, ex);
        emitSnoop(message + " (" + ex.getMessage() + ")");
        emit(Severity.ERROR, message, Attributes.of(
            AttributeKey.stringKey("throwable_message"), ex.getMessage(),
            AttributeKey.stringKey("throwable_type"), ex.getClass().getSimpleName(),
            AttributeKey.stringKey("throwable_stack_trace"), getStackTrace(ex))
        );
    }

    private static void emitSnoop(String msg) {
        if (!Cytosis.CONTEXT.isSendErrorsThroughSnooper()) return;
        Component component = Msg.redSplash("Error on server " + Cytosis.CONTEXT.SERVER_ID, "<newline> %s", msg);
        try {
            Cytosis.get(SnooperManager.class).sendSnoop(Snoops.SERVER_ERROR, Msg.snoop(component));
        } catch (NullPointerException ignored) { // Snooper isn't initialized Yet
            Logger.warn("Failed to log error via snooper!");
        }
    }

    private static void emit(Severity severity, String msg, @Nullable Attributes attribs) {
        if (!(Cytosis.CONTEXT.isMetricsEnabled())) return;
        Cytosis.get(CytosisOpenTelemetry.class)
            .getLogger().get("DefaultLogger").logRecordBuilder().setSeverity(severity)
            .setBody(msg)
            .setAttribute(AttributeKey.stringKey("server_id"), Cytosis.CONTEXT.SERVER_ID)
            .setAllAttributes(attribs)
            .emit();
    }

    private static String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : t.getStackTrace()) {
            sb.append(ste).append("\n");
        }
        return sb.toString();
    }
}