package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.text.Component;

import java.util.UUID;

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
        return Msg.badge("SNOOP! ", "F873F9").append(Msg.mm("<!i><!b>").append(component));
    }

    public static Component toTarget(UUID uuid) {
        return Cytosis.getCytonicNetwork().getCachedPlayerRanks().get(uuid).getPrefix()
                .append(Component.text(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid)));
    }
}
