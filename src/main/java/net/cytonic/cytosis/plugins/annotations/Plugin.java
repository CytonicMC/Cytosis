package net.cytonic.cytosis.plugins.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to help with plugin loading
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Plugin {

    /**
     * The name of the plugin
     *
     * @return the name
     */
    String name();

    /**
     * The dependencies of the plugin
     *
     * @return the dependencies
     */
    Dependency[] dependencies() default {};


    /**
     * The description of the plugin
     *
     * @return the description
     */
    String description() default "";

    /**
     * The version of the plugin
     *
     * @return plugin version
     */
    String version();

    /**
     * The author(s) of the plugin
     *
     * @return the author
     */
    String[] authors() default "";
}
