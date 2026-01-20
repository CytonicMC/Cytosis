package net.cytonic.protocol.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotifyHandler {

    /**
     * Sets the subject
     * <p>
     * This will only work if the ProtocolObject's first parameter is a {@code String}
     * <p>
     * It will be an empty string if not set
     *
     * @return The subject
     */
    String subject() default "";
}