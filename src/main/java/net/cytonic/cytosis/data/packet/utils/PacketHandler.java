package net.cytonic.cytosis.data.packet.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PacketHandler {

    /**
     * The NATS subject suffix to listen on (will be prefixed with environment prefix).
     * <p>
     * If empty, uses the packet's default channel (class simple name).
     * <p>
     * Example: "players.kick" becomes "dev.players.kick" in dev environment
     */
    String subject() default "";
}