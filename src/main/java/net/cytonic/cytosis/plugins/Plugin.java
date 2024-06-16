package net.cytonic.cytosis.plugins;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to help with plugin loading
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    /**
     * The name of the plugin
     *
     * @return the name
     */
    String name();

    /**
     * The dependencies of the plugin
     * @return the dependencies
     */
    String[] dependencies() default {};

    /**
     * The description of the plugin
     * @return the description
     */
    String description() default "";

    /**
     * The version of the plugin
     * @return plugin version
     */
    String version();

    /**
     * The author(s) of the plugin
     * @return the author
     */
    String author() default "";
}
