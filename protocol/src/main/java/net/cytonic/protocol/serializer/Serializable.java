package net.cytonic.protocol.serializer;

import com.google.gson.JsonParser;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;

public interface Serializable<T> {

    Codec<T> getCodec();

    default String serializeToString(T message) {
        return getCodec().encode(Transcoder.JSON, message).orElseThrow().toString();
    }

    default T deserializeFromString(String string) {
        return getCodec().decode(Transcoder.JSON, JsonParser.parseString(string)).orElseThrow();
    }
}
