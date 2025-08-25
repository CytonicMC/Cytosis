package net.cytonic.cytosis.bootstrap.mixins;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;

import java.util.Objects;

public record CytosisMixinLogger(@NotNull String id) implements ILogger {

    @NotNull
    private static final Logger SLF4J_LOGGER = Objects.requireNonNull(LoggerFactory.getLogger(CytosisMixinLogger.class));

    @Override
    public void catching(Level level, Throwable t) {
        this.log(level, "A throwable has been caught by the mixin subsystem: No further information.", t);
    }

    @Override
    public void catching(Throwable t) {


        CytosisMixinLogger.SLF4J_LOGGER.error("A throwable has been caught by the mixin subsystem: No further information.", t);
    }

    @Override
    public void debug(String message, Object... params) {
        CytosisMixinLogger.SLF4J_LOGGER.debug(message, params);
    }

    @Override
    public void debug(String message, Throwable t) {
        CytosisMixinLogger.SLF4J_LOGGER.debug(message, t);
    }

    @Override
    public void error(String message, Object... params) {
        CytosisMixinLogger.SLF4J_LOGGER.error(message, params);
    }

    @Override
    public void error(String message, Throwable t) {
        CytosisMixinLogger.SLF4J_LOGGER.error(message, t);
    }

    @Override
    public void fatal(String message, Object... params) {
        CytosisMixinLogger.SLF4J_LOGGER.error(message, params);
    }

    @Override
    public void fatal(String message, Throwable t) {
        CytosisMixinLogger.SLF4J_LOGGER.error(message, t);
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public String getType() {
        return "SLF4J (implemented by CytosisMixinLogger)";
    }

    @Override
    public void info(String message, Object... params) {
        CytosisMixinLogger.SLF4J_LOGGER.info(message, params);
    }

    @Override
    public void info(String message, Throwable t) {
        CytosisMixinLogger.SLF4J_LOGGER.info(message, t);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        switch (level) {
            case DEBUG:
                this.debug(message, params);
                break;
            case ERROR:
                this.error(message, params);
                break;
            case FATAL:
                this.fatal(message, params);
                break;
            case INFO:
                this.info(message, params);
                break;
            case TRACE:
                this.trace(message, params);
                break;
            case WARN:
                this.warn(message, params);
                break;
            default:
                CytosisMixinLogger.SLF4J_LOGGER.warn("Unknown logging level: {}", level);
                this.error(message, params);
                break;
        }
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        this.log(level, message, (Object) t);
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        return t;
    }

    @Override
    public void trace(String message, Object... params) {
        CytosisMixinLogger.SLF4J_LOGGER.trace(message, params);
    }

    @Override
    public void trace(String message, Throwable t) {
        CytosisMixinLogger.SLF4J_LOGGER.trace(message, t);
    }

    @Override
    public void warn(String message, Object... params) {
        CytosisMixinLogger.SLF4J_LOGGER.warn(message, params);
    }

    @Override
    public void warn(String message, Throwable t) {
        CytosisMixinLogger.SLF4J_LOGGER.warn(message, t);
    }
}