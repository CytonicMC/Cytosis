package net.cytonic.cytosis.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Locale;

import dev.minestomunited.common.config.Config;
import dev.minestomunited.common.config.ConfigSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies {@link Env}-annotated overrides on top of an already-resolved {@link Config} instance (or builds one from
 * scratch if every required leaf has an env var set).
 *
 * <p>Deliberately NOT a {@link ConfigSource} — sources only see a raw key and produce
 * a full document that fully replaces the previous source's result. This instead patches individual record components
 * on the already-resolved object, recursing into nested records via their canonical constructor, which is the only way
 * to do a genuine per-field override without every other required field also being present.
 */
public final class EnvOverride {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvOverride.class);

    private EnvOverride() {
    }

    /**
     * @param type the config (or nested record) type
     * @param base the already-resolved instance to patch, or {@code null} if none
     * @param <C>  the type
     * @return the patched instance, a freshly-built instance if {@code base} was {@code null} but every leaf had an env
     * override, or {@code base} unchanged if nothing applied.
     */
    @Nullable
    public static <C> C apply(Class<C> type, @Nullable C base) {
        return applyRecursive("MINESTOM_", type, base);
    }

    /**
     * @param type the config (or nested record) type
     * @param base the already-resolved instance to patch, or {@code null} if none
     * @param <C>  the type
     * @return the patched instance, a freshly-built instance if {@code base} was {@code null} but every leaf had an env
     * override, or {@code base} unchanged if nothing applied.
     */
    @Nullable
    public static <C> C apply(String prefix, Class<C> type, @Nullable C base) {
        return applyRecursive(prefix.replace('.', '_'), type, base);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <C> C applyRecursive(String pathPrefix, Class<C> type, @Nullable C base) {
        if (!type.isRecord()) {
            // leaf handled by caller
            return base;
        }

        RecordComponent[] components = type.getRecordComponents();
        Object[] args = new Object[components.length];
        boolean anyOverride = false;
        boolean canFullyConstruct = base != null;

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            String segment = segmentName(component);
            String path = pathPrefix + "_" + segment;

            Object currentValue = base == null ? null : invokeAccessor(component, base);

            if (component.getType().isRecord()) {
                Object nested = applyRecursive(path, (Class<C>) component.getType(), (C) currentValue);
                if (nested != currentValue) {
                    anyOverride = true;
                }
                if (nested == null && currentValue == null) {
                    canFullyConstruct = false;
                }
                args[i] = nested;
                continue;
            }

            String raw = System.getenv(path);
            if (raw != null) {
                anyOverride = true;
                args[i] = coerce(raw, component.getType(), path);
            } else {
                args[i] = currentValue;
                if (currentValue == null && isPrimitiveLike(component.getType())) {
                    // no base, no env var, and this leaf is required — can't construct from scratch
                    canFullyConstruct = false;
                }
            }
        }

        if (!anyOverride) {
            return base;
        }
        if (base == null && !canFullyConstruct) {
            LOGGER.warn("Partial @Env override found for {} but no base config to patch onto "
                + "and not every field is set via env vars — ignoring", type.getSimpleName());
            return null;
        }

        try {
            Class<?>[] paramTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
            }
            Constructor<C> ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to apply @Env overrides to {} — leaving as-is", type.getSimpleName(), e);
            return base;
        }
    }

    private static Object invokeAccessor(RecordComponent component, Object instance) {
        try {
            return component.getAccessor().invoke(instance);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read record component " + component.getName(), e);
        }
    }

    private static String segmentName(RecordComponent component) {
        @Nullable Env annotation = component.getAnnotation(Env.class);
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        // camelCase -> HYPHEN-CASE
        String name = component.getName();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('-');
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

    private static boolean isPrimitiveLike(Class<?> type) {
        return type.isPrimitive();
    }

    private static Object coerce(String raw, Class<?> type, String path) {
        try {
            if (type == String.class) {
                return raw;
            }
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(raw);
            }
            if (type == long.class || type == Long.class) {
                return Long.parseLong(raw);
            }
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(raw);
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(raw);
            }
            if (type.isEnum()) {
                //noinspection unchecked,rawtypes
                return Enum.valueOf((Class<Enum>) type, raw.toUpperCase(Locale.ROOT));
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to coerce env var {}={} to {} — leaving unset", path, raw,
                type.getSimpleName(), e);
        }
        LOGGER.warn("Unsupported @Env field type {} for {} — leaving unset", type.getSimpleName(), path);
        return null;
    }
}