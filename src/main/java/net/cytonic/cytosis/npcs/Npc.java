package net.cytonic.cytosis.npcs;

import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.Taggable;

/**
 * The blueprint for an NPC
 */
public interface Npc extends Taggable {

    Tag<String> DATA_TAG = Tag.String("npc_data");

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
     *
     * @param npc The npc to import data from
     * @return the created builder
     */
    static HumanoidBuilder ofHumanoid(Humanoid npc) {
        return new HumanoidBuilder(npc);
    }

    /**
     * Adds an interaction to the NPC
     *
     * @param action the exection action
     */
    void addAction(NpcAction action);

    /**
     * Lists the NPCs actions
     *
     * @return the NPC's actions
     */
    List<NpcAction> getActions();

    /**
     * Gets the hologram lines
     *
     * @return The NPC's hologram lines
     */
    List<Component> getLines();

    /**
     * Sets the mutli line hologram contents
     *
     * @param lines The lines of the hologram
     */
    void setLines(Component... lines);

    /**
     * If the NPC is glowing
     *
     * @return The NPC's glowing state
     */
    boolean isGlowing();

    /**
     * Sets the NPC to be glowing
     *
     * @param color the color to glow
     */
    void setGlowing(NamedTextColor color);

    /**
     * The color the NPC glows.
     * <p>
     * Returns {@link NamedTextColor#WHITE} if the NPC is not glowing
     *
     * @return the NPC's glowing color
     */
    NamedTextColor getGlowingColor();

    /**
     * Gets the NPC's UUID
     *
     * @return the uuid
     */
    UUID getUuid();

    /**
     * Creates the holograms
     */
    void createHolograms();

    /**
     * Removes the NPC from the world.
     */
    void remove();
}
