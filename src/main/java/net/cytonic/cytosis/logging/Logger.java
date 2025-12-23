package net.cytonic.cytosis.logging;

/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementaion'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.managers.SnooperManager;
import net.cytonic.cytosis.utils.Msg;

/**
 * The logger interface
 */
public interface Logger {

    /**
     * The logger instance
     */
    ExtendedLogger LOGGER = LogManager.getContext(false).getLogger("Cytosis");

    // OpenTelemetry Logger
    io.opentelemetry.api.logs.Logger OTEL_LOGGER = GlobalOpenTelemetry.get().getLogsBridge().get("Cytosis");

    /**
     * Logs a debug message
     *
     * @param message The message to log
     * @param args    The arguments to format the message
     */
    static void debug(String message, Object... args) {
        // shut, that causes it to call a differnt method
        LOGGER.atLevel(LogLevel.CYTOSIS_DEBUG).log("\u001B[0;95m" + message.formatted(args));
        if (Cytosis.CONTEXT.isMetricsEnabled()) {
            Span span = Span.current();
            OTEL_LOGGER.logRecordBuilder().setSeverity(Severity.DEBUG).setBody(message)
                .setAttribute(AttributeKey.stringKey("server_id"), CytosisContext.SERVER_ID)
                .setAttribute(AttributeKey.stringKey("trace_id"), span.getSpanContext()
                    .getTraceId()) // Add trace ID
                .setAttribute(AttributeKey.stringKey("span_id"), span.getSpanContext()
                    .getSpanId())   // Add span ID
                .emit();
        }
    }

    /**
     * Logs a message in the INFO level
     *
     * @param message The message to log
     * @param args    The args to format the message
     */
    static void info(String message, Object... args) {
        LOGGER.info(message.formatted(args));
        if (Cytosis.CONTEXT.isMetricsEnabled()) {
            Span span = Span.current();
            OTEL_LOGGER.logRecordBuilder()
                .setSeverity(Severity.INFO)
                .setBody(message)
                .setAttribute(AttributeKey.stringKey("server_id"), CytosisContext.SERVER_ID)
                .setAttribute(AttributeKey.stringKey("trace_id"), span.getSpanContext().getTraceId()) // Add trace ID
                .setAttribute(AttributeKey.stringKey("span_id"), span.getSpanContext().getSpanId())   // Add span ID
                .emit();
        }
    }

    /**
     * Logs a warning
     *
     * @param message The message to log
     * @param args    The arguments to format the message
     */
    static void warn(String message, Object... args) {
        LOGGER.warn(message.formatted(args));
        if (Cytosis.CONTEXT.isMetricsEnabled()) {
            Span span = Span.current();
            OTEL_LOGGER.logRecordBuilder()
                .setSeverity(Severity.WARN)
                .setBody(message)
                .setAttribute(AttributeKey.stringKey("server_id"), CytosisContext.SERVER_ID)
                .setAttribute(AttributeKey.stringKey("trace_id"), span.getSpanContext().getTraceId()) // Add trace ID
                .setAttribute(AttributeKey.stringKey("span_id"), span.getSpanContext().getSpanId())   // Add span ID
                .emit();
        }
    }

    /**
     * Logs an error message in the ERROR level
     *
     * @param message the message to log
     * @param args    the arguments used to format the message
     */
    static void error(String message, Object... args) {

        LOGGER.error(message.formatted(args));
        Component component = Msg.red("""
                <b>Error Logged on server '" + CytosisContext.SERVER_ID + "'</b></red><newline><gray> Message: %s""",
            message);
        try {
            Cytosis.get(SnooperManager.class)
                .sendSnoop(CytosisSnoops.SERVER_ERROR, Msg.snoop(component));
        } catch (NullPointerException ignored) { // Snooper isn't initialized Yet
            Logger.warn("Failed to log error via snooper!");
        }
        if (Cytosis.CONTEXT.isMetricsEnabled()) {
            Span span = Span.current();
            OTEL_LOGGER.logRecordBuilder().setSeverity(Severity.ERROR).setBody(message)
                .setAttribute(AttributeKey.stringKey("server_id"), CytosisContext.SERVER_ID)
                .setAttribute(AttributeKey.stringKey("trace_id"), span.getSpanContext()
                    .getTraceId()) // Add trace ID
                .setAttribute(AttributeKey.stringKey("span_id"), span.getSpanContext()
                    .getSpanId())   // Add span ID
                .emit();
        }
    }

    /**
     * Logs an error with a throwable
     *
     * @param message the message to log
     * @param ex      the throwable to log
     */
    static void error(String message, Throwable ex) {
        Component component = Msg.mm(
            "<red><b>Error Logged on server '" + CytosisContext.SERVER_ID + "'</b></red><newline><gray> Message: "
                + message
                + "</gray><newline><red><b>Throwable:<b></red><gray> " + ex.getMessage());
        try {
            Cytosis.get(SnooperManager.class)
                .sendSnoop(CytosisSnoops.SERVER_ERROR, Msg.snoop(component));
        } catch (NullPointerException ignored) { // Snooper isn't initialized Yet
            Logger.warn("Failed to log error via snooper!");
        }
        LOGGER.error(message, ex);
        if (Cytosis.CONTEXT.isMetricsEnabled()) {
            Span span = Span.current();
            OTEL_LOGGER.logRecordBuilder().setSeverity(Severity.ERROR).setBody(message)
                .setAttribute(AttributeKey.stringKey("server_id"), CytosisContext.SERVER_ID)
                .setAttribute(AttributeKey.stringKey("throwable_message"), ex.getMessage())
                .setAttribute(AttributeKey.stringKey("throwable_type"), ex.getClass().getSimpleName())
                .setAttribute(AttributeKey.stringKey("throwable_stack_trace"), getStackTrace(ex))
                .setAttribute(AttributeKey.stringKey("trace_id"), span.getSpanContext()
                    .getTraceId()) // Add trace ID
                .setAttribute(AttributeKey.stringKey("span_id"), span.getSpanContext()
                    .getSpanId())   // Add span ID
                .emit();
        }
    }

    private static String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : t.getStackTrace()) {
            sb.append(ste).append("\n");
        }
        return sb.toString();
    }
}