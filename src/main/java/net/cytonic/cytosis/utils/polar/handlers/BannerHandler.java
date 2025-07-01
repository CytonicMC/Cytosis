package net.cytonic.cytosis.utils.polar.handlers;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BannerHandler implements BlockHandler {
    private final Key key;

    private BannerHandler(String name) {
        key = Key.key(name);
        MinecraftServer.getBlockManager().registerHandler(key, () -> this);
    }

    public static void setup() {
        new BannerHandler("minecraft:banner");
    }

    @Override
    public @NotNull Key getKey() {
        return key;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.String("CustomName"),
                Tag.NBT("patterns").list()
        );
    }
}
