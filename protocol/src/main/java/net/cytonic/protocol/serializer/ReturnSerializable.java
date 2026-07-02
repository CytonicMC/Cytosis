package net.cytonic.protocol.serializer;

import com.google.gson.JsonParser;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;

public interface ReturnSerializable<T> {

    Codec<T> getReturnCodec();

    default String serializeReturnToString(T message) {
        return getReturnCodec().encode(Transcoder.JSON, message).orElseThrow().toString();
    }

    default T deserializeReturnFromString(String string) {
        return getReturnCodec().decode(Transcoder.JSON, JsonParser.parseString(string)).orElseThrow();
    }
}
