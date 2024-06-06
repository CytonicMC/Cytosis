package net.cytonic.cytosis.plugins;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    String name();

    String[] dependencies() default {};

    String description() default "";

    String version();

    String author() default "";

}
