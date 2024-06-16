package net.cytonic.cytosis.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.UUID;

/**
 * The blueprint for an NPC
 */
public interface NPC {

    /**
     * Creates a Humanoid NPC builder
     *
     * @param pos               The pos to spawn the NPC at
     * @param instanceContainer The instance to spawn the NPC in
     * @return The builder
     */
    // builders
    static HumanoidBuilder ofHumanoid(Pos pos, Instance instanceContainer) {
        return new HumanoidBuilder(pos, instanceContainer);
    }

    /**
     * Creates a new builder out of an existing NPC
     * @param NPC The npc to import data from
     * @return the created builder
     */
    static HumanoidBuilder ofHumanoid(Humanoid NPC) {
        return new HumanoidBuilder(NPC);
    }

    /**
     * Sets the mutli line hologram contents
     * @param lines The lines of the hologram
     */
    void setLines(Component... lines);

    /**
     * Adds an interaction to the NPC
     * @param action the exection action
     */
    void addAction(NPCAction action);

    /**
     * Sets the NPC to be glowing
     * @param color the color to glow
     */
    void setGlowing(NamedTextColor color);

    /**
     * Lists the NPCs actions
     * @return the NPC's actions
     */
    List<NPCAction> getActions();

    /**
     * Gets the hologram lines
     * @return The NPC's hologram lines
     */
    List<Component> getLines();

    /**
     * If the NPC is glowing
     * @return The NPC's glowing state
     */
    boolean isGlowing();

    /**
     * The color the NPC glows.
     * <p>
     * Returns {@link NamedTextColor#WHITE} if the NPC is not glowing
     * @return the NPC's glowing color
     */
    NamedTextColor getGlowingColor();

    /**
     * Gets the NPC's UUID
     * @return the uuid
     */
    UUID getUUID();

    /**
     * Creates the holograms
     */
    void createHolograms();
}
