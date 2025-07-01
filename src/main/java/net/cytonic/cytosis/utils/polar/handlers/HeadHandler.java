package net.cytonic.cytosis.utils.polar.handlers;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class HeadHandler implements BlockHandler {
    private final Key key;

    private HeadHandler(String name) {
        key = Key.key(name);
        MinecraftServer.getBlockManager().registerHandler(key, () -> this);
    }

    public static void setup() {
        new HeadHandler("minecraft:player_head");
    }

    @Override
    public @NotNull Key getKey() {
        return key;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.String("custom_name"),
                Tag.String("note_block_sound"),
                Tag.NBT("profile")
        );
    }
}
