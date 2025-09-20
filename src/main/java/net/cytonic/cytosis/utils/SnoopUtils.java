package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class SnoopUtils {
    public static Component toTarget(UUID uuid) {
        CytonicNetwork network = Cytosis.CONTEXT.getComponent(CytonicNetwork.class);
        return network.getCachedPlayerRanks().get(uuid).getPrefix()
                .append(Component.text(network.getLifetimePlayers().getByKey(uuid)));
    }
}