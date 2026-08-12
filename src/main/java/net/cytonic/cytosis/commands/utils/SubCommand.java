package net.cytonic.cytosis.commands.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubCommand {

    /**
     * The parent command of this subcommand.
     *
     * @return the class to register this command to
     */
    Class<? extends CytosisCommand> value();
}
