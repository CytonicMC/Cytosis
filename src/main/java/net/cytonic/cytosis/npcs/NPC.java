package net.cytonic.cytosis.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.UUID;

public interface NPC {

    // builders
    static HumanoidBuilder ofHumanoid(Pos pos, Instance instanceContainer) {
        return new HumanoidBuilder(pos, instanceContainer);
    }

    static HumanoidBuilder ofHumanoid(Humanoid NPC) {
        return new HumanoidBuilder(NPC);
    }

    void setLines(Component... lines);

    void addAction(NPCAction action);

    void setGlowing(NamedTextColor color);

    List<NPCAction> getActions();

    List<Component> getLines();

    boolean isGlowing();

    NamedTextColor getGlowingColor();

    UUID getUUID();

    void createHolograms();
}
