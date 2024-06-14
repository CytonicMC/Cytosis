package net.cytonic.cytosis.npcs;

import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.instance.Instance;

import java.util.UUID;

public class HumanoidBuilder {
    private final Pos pos;
    private final Instance instanceContainer;
    private final Humanoid NPC;

    protected HumanoidBuilder(Pos pos, Instance instanceContainer) {
        this.pos = pos;
        this.instanceContainer = instanceContainer;
        this.NPC = new Humanoid(UUID.randomUUID());
    }

    protected HumanoidBuilder(Humanoid NPC) {
        this.NPC = NPC;
        this.pos = NPC.getPosition();
        this.instanceContainer = NPC.getInstance();
    }

    public HumanoidBuilder skin(PlayerSkin skin) {
        NPC.setSkin(skin);
        return this;
    }

    public HumanoidBuilder skin(String value, String signature) {
        this.skin(new PlayerSkin(value, signature));
        return this;
    }

    public HumanoidBuilder lines(Component... lines) {
        NPC.setLines(lines);
        return this;
    }

    public HumanoidBuilder interactTrigger(NPCAction action) {
        NPC.addAction(action);
        return this;
    }

    public HumanoidBuilder glowing(NamedTextColor color) {
        NPC.setGlowing(color);
        return this;
    }

    public Humanoid build() {
        NPC.setInstance(instanceContainer, pos);
        NPC.createHolograms();
        Cytosis.getNpcManager().addNPC(NPC);
        return NPC;
    }
}
