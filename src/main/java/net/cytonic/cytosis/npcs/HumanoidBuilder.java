package net.cytonic.cytosis.npcs;

import net.cytonic.cytosis.Cytosis;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.instance.Instance;

import java.util.UUID;

/**
 * A builder for creating Humanoid NPCs
 */
public class HumanoidBuilder {
    private final Pos pos;
    private final Instance instanceContainer;
    private final Humanoid NPC;

    /**
     * A contructor for creating a  builder
     *
     * @param pos               The pos to spawn the NPC at
     * @param instanceContainer The instance to spawn the NPC in
     */
    protected HumanoidBuilder(Pos pos, Instance instanceContainer) {
        this.pos = pos;
        this.instanceContainer = instanceContainer;
        this.NPC = new Humanoid(UUID.randomUUID());
    }

    /**
     * The constructor for creating a  builder from an existing NPC
     * @param NPC THe NPC to import data from
     */
    protected HumanoidBuilder(Humanoid NPC) {
        this.NPC = NPC;
        this.pos = NPC.getPosition();
        this.instanceContainer = NPC.getInstance();
    }

    /**
     * Sets the skin
     * @param skin The skin data
     * @return The builder with updated data
     */
    public HumanoidBuilder skin(PlayerSkin skin) {
        NPC.setSkin(skin);
        return this;
    }

    /**
     * Sets the skin
     * @param signature The signature of the skin
     * @param value The value of the skin
     * @return The builder with updated data
     */
    public HumanoidBuilder skin(String value, String signature) {
        this.skin(new PlayerSkin(value, signature));
        return this;
    }

    /**
     * Sets the NPC's multi-line hologram contents
     * @param lines The lines of the hologram
     * @return The builder with updated data
     */
    public HumanoidBuilder lines(Component... lines) {
        NPC.setLines(lines);
        return this;
    }

    /**
     * Adds an interaction trigger to run an action on interact
     * @param action The action to run
     * @return The builder with updated data
     */
    public HumanoidBuilder interactTrigger(NPCAction action) {
        NPC.addAction(action);
        return this;
    }

    /**
     * Makes the NPC glow
     * @param color The color to glow
     * @return The builder with updated data
     */
    public HumanoidBuilder glowing(NamedTextColor color) {
        NPC.setGlowing(color);
        return this;
    }

    /**
     * Builds the NPC and creates the holograms
     * @return The Humanoid NPC
     */
    public Humanoid build() {
        NPC.setInstance(instanceContainer, pos);
        NPC.createHolograms();
        Cytosis.getNpcManager().addNPC(NPC);
        return NPC;
    }
}
