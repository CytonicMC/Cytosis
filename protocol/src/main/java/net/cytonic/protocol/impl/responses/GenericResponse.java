package net.cytonic.protocol.impl.responses;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public record GenericResponse(boolean success, String message) {

    public static final Codec<GenericResponse> CODEC = StructCodec.struct(
        "success", Codec.BOOLEAN, GenericResponse::success,
        "message", Codec.STRING, GenericResponse::message,
        GenericResponse::new
    );
}
