package net.cytonic.cytosis.bootstrap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that represents a Cytosis component within the system.
 * Cytosis components can specify a priority value to determine the order in which
 * they are processed and a list of dependencies on other components.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CytosisComponent {
    int priority() default 50;

    Class<?>[] dependsOn() default {};
}