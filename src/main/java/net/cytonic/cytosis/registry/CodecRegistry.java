package net.cytonic.cytosis.registry;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.json.JsonFileUtils;
import net.cytonic.cytosis.logging.Logger;

public abstract class CodecRegistry<T extends Keyed> extends Registry<T> {

    private final Codec<T> codec;
    private final String path;
    private final List<String> extraPaths;

    public CodecRegistry(Key id, Codec<T> codec, String path) {
        this(id, codec, path, new ArrayList<>());
    }

    public CodecRegistry(Key id, Codec<T> codec, String path, List<String> extraPaths) {
        super(id);
        this.codec = codec;
        this.path = path;
        this.extraPaths = extraPaths;
        reload();
    }

    protected void reload() {
        objects.clear();
        if (Cytosis.class.getResource(path) != null) {
            for (T item : JsonFileUtils.parseFilesWithCodec(codec, path)) {
                add(item.key(), item);
            }
        }
        for (String extraPath : extraPaths) {
            if (Cytosis.class.getResource(extraPath) == null) continue;
            for (T item : JsonFileUtils.parseFilesWithCodec(codec, extraPath)) {
                add(item.key(), item);
            }
        }
        Logger.info("Found %d of %s", objects.size(), id.asString());
    }
}
