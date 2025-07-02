package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class SnoopUtils {

    public static Component toTarget(UUID uuid) {
        return Cytosis.getCytonicNetwork().getCachedPlayerRanks().get(uuid).getPrefix()
                .append(Component.text(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid)));
    }
}
