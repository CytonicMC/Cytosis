package net.cytonic.cytosis.utils;

import net.kyori.adventure.text.Component;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class SnoopUtils {

    //todo: add different colors
    //todo: randomize the snoop prefix
    // some ideas: SNOOPED! SNOOPPLED! idfk

    /**
     * Applies the {@code SNOOP!} to the message
     *
     * @param component The snoop message
     * @return The snoopified component
     */
    public static Component toSnoop(Component component) {
        return MM."<#F873F9><b>SNOOP!</b></#F873F9> ".append(component);
    }
}
