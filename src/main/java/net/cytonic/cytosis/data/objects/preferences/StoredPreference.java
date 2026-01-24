package net.cytonic.cytosis.data.objects.preferences;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

/**
 * A class that represents a preference that is created by something else, which is safe to store.
 *
 */
@Getter
public class StoredPreference extends Preference<String> {

    public static final Codec<StoredPreference> CODEC = StructCodec.struct(
        "key", Codec.KEY, Preference::getKey,
        "val", Codec.STRING, Preference::getValue,
        StoredPreference::new
    );

    public StoredPreference(Key namespace, String rawValue) {
        super(String.class, namespace, rawValue);
    }
}
