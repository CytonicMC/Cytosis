package net.cytonic.cytosis.npcs;

import lombok.Setter;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class representing a Player NPC. To create one use the builder methods in {@link NPC}
 */
@SuppressWarnings("UnstableApiUsage")
public class Humanoid extends EntityCreature implements NPC {

    private final String username;
    private final List<NPCAction> actions = new ArrayList<>();
    private List<Component> lines = new ArrayList<>();
    @Setter
    private PlayerSkin skin;
    private boolean glowing = false;
    private NamedTextColor glowingColor = NamedTextColor.WHITE;

    /**
     * Creates a new Humanoid from uuid, username, and skin
     *
     * @param uuid     The NPC's UUID
     * @param username The NPC's username (pretty irrelevent)
     * @param skin     The NPC's skin data
     */
    public Humanoid(UUID uuid, String username, PlayerSkin skin) {
        super(EntityType.PLAYER, uuid);
        this.username = username;
        this.skin = skin;
    }

    /**
     * Creates a new NPC from a UUID
     *
     * @param uuid The UUID to use
     */
    public Humanoid(UUID uuid) {
        super(EntityType.PLAYER, uuid);
        this.username = "npc-" + uuid.toString().substring(0, 12) + "";
        this.skin = null;
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        // creating the player
        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
        if (skin.textures() != null && skin.signature() != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()));
        }
        var entry = new PlayerInfoUpdatePacket.Entry(getUuid(), username, properties, false,
                0, GameMode.SURVIVAL, null, null, 0);
        player.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry));
        super.updateNewViewer(player);
        player.sendPackets(new EntityMetaDataPacket(getEntityId(), Map.of(17, Metadata.Byte((byte) 127))));

        var team = new TeamsPacket("NPC-" + getUuid().toString() + "",
                new TeamsPacket.CreateTeamAction(Component.empty(),
                        (byte) 0x0, TeamsPacket.NameTagVisibility.NEVER,
                        TeamsPacket.CollisionRule.NEVER, glowingColor,
                        Component.empty(), Component.empty(), Utils.list(username)));

        player.sendPacket(team);
        // glowing
        if (glowing) {
            super.editEntityMeta(PlayerMeta.class, meta -> meta.setHasGlowingEffect(true));
        }
    }

    @Override
    public void updateOldViewer(@NotNull Player player) {
        super.updateOldViewer(player);
        player.sendPacket(new PlayerInfoRemovePacket(getUuid()));
    }

    @Override
    public void tick(long time) {
        super.tick(time);
    }

    @Override
    public void update(long time) {
        super.update(time);
    }

    @Override
    public void addAction(NPCAction action) {
        actions.add(action);
    }

    @Override
    public void setGlowing(NamedTextColor color) {
        glowing = true;
        glowingColor = color;
    }

    @Override
    public List<NPCAction> getActions() {
        return actions;
    }

    @Override
    public List<Component> getLines() {
        return lines;
    }

    @Override
    public void setLines(Component... lines) {
        this.lines.clear();
        this.lines.addAll(Utils.list(lines));
        this.lines = this.lines.reversed();
    }

    @Override
    public NamedTextColor getGlowingColor() {
        return glowingColor;
    }

    @Override
    public UUID getUUID() {
        return super.getUuid();
    }

    @Override
    public void createHolograms() {
        final double spacing = 0.3;
        for (int i = 0; i < lines.size(); i++) {
            Entity hologram = new Entity(EntityType.TEXT_DISPLAY);
            int finalI = i;
            hologram.editEntityMeta(TextDisplayMeta.class, meta -> {
                meta.setText(lines.get(finalI));
                meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
                meta.setHasNoGravity(true);
            });

            Pos pos = position.add(0, (i * spacing) + 2.05, 0);

            hologram.setInstance(instance, pos);
            hologram.teleport(pos);
        }
    }
}