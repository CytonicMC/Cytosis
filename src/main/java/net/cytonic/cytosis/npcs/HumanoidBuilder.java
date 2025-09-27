package net.cytonic.cytosis.npcs;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.managers.NpcManager;

/**
 * A builder for creating Humanoid NPCs, to use it call {@link  Npc#ofHumanoid(Humanoid)} or
 * {@link Npc#ofHumanoid(Pos, Instance)}
 */
public class HumanoidBuilder {

    private final Pos pos;
    private final Instance instanceContainer;
    private final Humanoid npc;

    /**
     * A constructor for creating a  builder
     *
     * @param pos               The pos to spawn the NPC at
     * @param instanceContainer The instance to spawn the NPC in
     */
    protected HumanoidBuilder(Pos pos, Instance instanceContainer) {
        this.pos = pos;
        this.instanceContainer = instanceContainer;
        this.npc = new Humanoid(UUID.randomUUID());
    }

    /**
     * The constructor for creating a  builder from an existing NPC
     *
     * @param npc THe NPC to import data from
     */
    protected HumanoidBuilder(Humanoid npc) {
        this.npc = npc;
        this.pos = npc.getPosition();
        this.instanceContainer = npc.getInstance();
    }

    /**
     * Sets the skin
     *
     * @param signature The signature of the skin
     * @param value     The value of the skin
     * @return The builder with updated data
     */
    public HumanoidBuilder skin(String value, String signature) {
        return this.skin(new PlayerSkin(value, signature));
    }

    /**
     * Sets the skin
     *
     * @param skin The skin data
     * @return The builder with updated data
     */
    public HumanoidBuilder skin(PlayerSkin skin) {
        npc.setSkin(skin);
        return this;
    }

    /**
     * Sets the skin to a random default skin like steve or alex
     *
     * @return the builder for chaining
     */
    public HumanoidBuilder defaultSkin() {
        return this.skin(new PlayerSkin(null, null));
    }

    /**
     * Makes this NPC invulnerable, or unable to be damaged
     *
     * @return the builder, for chaining
     */
    public HumanoidBuilder invulnerable() {
        return this.invulnerable(true);
    }

    /**
     * Makes this NPC either able to be damaged or unable to be damaged
     *
     * @param invulnerable if this npc should be immune to damage
     * @return the builder, for chaining
     */
    public HumanoidBuilder invulnerable(boolean invulnerable) {
        npc.setInvulnerable(invulnerable);
        return this;
    }

    /**
     * Tags this NPC with the given string value. Repeating this tag overwrites any previously written data
     *
     * @param tag The string data to associate with the NPC
     * @return The builder, for chaining
     */
    public HumanoidBuilder tag(String tag) {
        return tag(Npc.DATA_TAG, tag);
    }

    /**
     * Tags the NPC with the given Tag and corresponding value. Calling this method multiple times for the same tag,
     * overwrites existing data.
     *
     * @param tag   The tag to tag the NPC with
     * @param value the value to associate with the tag
     * @param <T>   The type of the tag.
     * @return the builder, for chaining.
     */
    public <T> HumanoidBuilder tag(Tag<T> tag, T value) {
        npc.setTag(tag, value);
        return this;
    }

    /**
     * Sets the NPC's multi-line hologram contents
     *
     * @param lines The lines of the hologram
     * @return The builder with updated data
     */
    public HumanoidBuilder lines(Component... lines) {
        npc.setLines(lines);
        return this;
    }

    /**
     * Adds an interaction trigger to run an action on interact
     *
     * @param action The action to run
     * @return The builder with updated data
     */
    public HumanoidBuilder interactTrigger(NpcAction action) {
        npc.addAction(action);
        return this;
    }

    /**
     * Makes the NPC glow
     *
     * @param color The color to glow
     * @return The builder with updated data
     */
    public HumanoidBuilder glowing(NamedTextColor color) {
        npc.setGlowing(color);
        return this;
    }

    /**
     * Builds the NPC and creates the holograms
     *
     * @return The Humanoid NPC
     */
    public Humanoid build() {
        npc.setInstance(instanceContainer, pos);
        npc.createHolograms();
        Cytosis.CONTEXT.getComponent(NpcManager.class).addNpc(npc);
        return npc;
    }
}