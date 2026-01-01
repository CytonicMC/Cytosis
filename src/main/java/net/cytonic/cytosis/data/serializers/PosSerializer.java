package net.cytonic.cytosis.data.serializers;

import java.lang.reflect.Type;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class PosSerializer implements TypeSerializer<Pos> {

    @Override
    public Pos deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String pos = node.getString();
        if (pos == null) {
            throw new SerializationException("Cannot deserialize a null pos!");
        }
        return net.cytonic.cytosis.utils.PosSerializer.deserialize(pos);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable Pos obj, @NotNull ConfigurationNode node)
        throws SerializationException {
        if (obj == null) {
            throw new SerializationException("Cannot serialize a null pos!");
        }
        node.set(net.cytonic.cytosis.utils.PosSerializer.serialize(obj));
    }
}
