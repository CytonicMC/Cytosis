package net.cytonic.cytosis.utils.polar.handlers;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SignHandler implements BlockHandler {
    private final Key key;

    private SignHandler(String name) {
        key = Key.key(name);
        MinecraftServer.getBlockManager().registerHandler(key, () -> this);
    }

    public static void setup() {
        new SignHandler("minecraft:sign");
        new SignHandler("minecraft:hanging_sign");
    }

    @Override
    public @NotNull Key getKey() {
        return key;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.Boolean("is_waxed"),
                Tag.NBT("front_text"),
                Tag.NBT("back_text")
        );
    }
}
