package net.cytonic.cytosis.data.serializers;

import java.lang.reflect.Type;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class KeySerializer implements TypeSerializer<Key> {

    @Override
    public Key deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String key = node.getString();
        if (key == null) {
            throw new SerializationException("Cannot deserialize a null key!");
        }
        return Key.key(key);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Key obj, @NotNull ConfigurationNode node)
        throws SerializationException {
        if (obj == null) {
            throw new SerializationException("Cannot serialize a null key!");
        }
        node.set(obj.asString());
    }
}
