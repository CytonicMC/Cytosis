package net.cytonic.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotifyHandler {

    Class<? extends ProtocolObject<?, ?>> value();

    /**
     * Sets the subject
     * <p>
     * This will only work if the ProtocolObject's first paramiter is a {@code String}
     * <p>
     * It will be an empty string if not set
     *
     * @return The subject
     */
    String subject() default "";
}