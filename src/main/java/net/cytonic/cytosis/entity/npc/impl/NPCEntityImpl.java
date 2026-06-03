package net.cytonic.cytosis.entity.npc.impl;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class NPCEntityImpl extends EntityCreature {

    private final String username;

    @Nullable
    private final PlayerSkin skin;
    private final List<Component> holograms;

    public NPCEntityImpl(
        String username,
        @Nullable PlayerSkin skin,
        @NotNull List<Component> holograms) {
        super(EntityType.PLAYER, UUID.randomUUID());
        this.username = username;

        this.skin = skin;
        this.holograms = holograms;
        setNoGravity(true);
    }
}