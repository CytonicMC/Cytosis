package net.cytonic.cytosis.entity.npc.impl;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class NPCEntityImpl extends EntityCreature {

    private final String username;

    private final String skinTexture;
    private final String skinSignature;
    private final List<Component> holograms;

    public NPCEntityImpl(
        String username,
        @Nullable String skinTexture,
        @Nullable String skinSignature,
        @NotNull List<Component> holograms) {
        super(EntityType.PLAYER, UUID.randomUUID());
        this.username = username;

        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
        this.holograms = holograms;
        setNoGravity(true);
    }
}