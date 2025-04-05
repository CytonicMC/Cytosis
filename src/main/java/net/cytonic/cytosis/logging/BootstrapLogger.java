package net.cytonic.cytosis.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;
import java.util.logging.Logger;

/**
 * The logging implementation intended for use with the bootstrapper
 */
public class BootstrapLogger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Logger LOGGER = Logger.getLogger("CytosisBootstrapper");

    static {

        LOGGER.setUseParentHandlers(false);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String time = LocalTime.now().format(TIME_FORMAT);
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("[%s] [%s] [Bootstrapper] -> %s%n", time, colorizeLevel(record.getLevel()), record.getMessage()));
                if (record.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    sb.append(sw);
                }
                return sb.toString();
            }
        });
        LOGGER.addHandler(consoleHandler);
    }

    public static void info(String message, Object... args) {
        LOGGER.log(Level.INFO, message, args);
    }

    public static void error(String message, Object... args) {
        LOGGER.log(Level.SEVERE, message, args);
    }

    public static void warn(String message, Object... args) {
        LOGGER.log(Level.WARNING, message, args);
    }

    public static void error(String message, Throwable t) {
        if (t instanceof InvocationTargetException target) {
            t = target.getCause();
        }
        LOGGER.log(Level.SEVERE, message, t);
    }

    private static String colorizeLevel(Level level) {
        String color = switch (level.getName()) {
            case "SEVERE" -> "\u001B[31m"; // Red
            case "WARNING" -> "\u001B[33m"; // Yellow
            case "INFO" -> "\u001B[32m"; // Green
            case "CONFIG", "FINE" -> "\u001B[36m"; // Cyan
            case "FINER", "FINEST" -> "\u001B[34m"; // Blue
            default -> "\u001B[0m"; // Reset
        };
        return color + level.getName() + "\u001B[0m";
    }
}
