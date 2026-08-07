package net.cytonic.cytosis.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.minestomunited.common.config.ConfigSource;

/**
 * Marks a {@link dev.minestomunited.common.config.Config} record component as overridable by an individual environment
 * variable, independent of whatever {@link ConfigSource} produced the base value.
 *
 * <p>By default the env var name is derived from the component name by
 * splitting camelCase word boundaries with a hyphen and uppercasing (e.g. {@code globalDatabase -> GLOBAL-DATABASE}).
 * {@link #value()} overrides just that segment.
 *
 * <p>Nested record components (e.g. {@code CytosisConfig.database.host}) are
 * addressed by joining each level's segment with a single underscore: {@code MINESTOM_CONFIG_DATABASE_HOST}. Underscore
 * is reserved for nesting — it is never introduced by the default derivation, only hyphens are.
 *
 * <pre>{@code
 * public record DatabaseConfig(
 *     String username,
 *     String password,
 *     String host,
 *     int port,
 *     String database,
 *     @Env("GLOBAL-DATABASE") String globalDatabase
 * ) {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Env {

    /**
     * Overrides the derived env var segment name for this component. Leave empty to use the default
     * camelCase-to-HYPHEN-CASE derivation.
     */
    String value() default "";
}