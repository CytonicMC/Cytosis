package net.cytonic.cytosis.entity.newnpx.configuration;

import java.util.List;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;

public interface NPCConfiguration {

    List<Component> holograms(CytosisPlayer player);

    Pos position(CytosisPlayer player);

    default boolean looking(CytosisPlayer player) {
        return false;
    }

    @Nullable
    default Component chatName() {
        return null;
    }

    default Instance instance() {
        return Cytosis.get(InstanceContainer.class);
    }

    @Nullable
    String texture(CytosisPlayer player);

    @Nullable
    String signature(CytosisPlayer player);
}
