package net.cytonic.protocol.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import org.jspecify.annotations.Nullable;

@UtilityClass
public class ProtocolCodecUtils {

    public static final Codec<Instant> INSTANT = Codec.STRING.transform(Instant::parse, Instant::toString);
    private static final DateTimeFormatter GO_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX");
    public static final Codec<Instant> GO_INSTANT = Codec.STRING.transform(
        string -> OffsetDateTime.parse(string).toInstant(),
        instant -> instant.atOffset(ZoneOffset.UTC).format(GO_FORMAT)
    );
    public static final Codec<Component> MINI_MESSAGE = Codec.STRING.transform(
        string -> MiniMessage.miniMessage().deserialize(string),
        component -> MiniMessage.miniMessage().serialize(component)
    );

    public static <T> Codec<T> Unit(T object) {
        return new Codec<>() {
            @Override
            public <D> Result<T> decode(Transcoder<D> coder, D value) {
                return new Result.Ok<>(object);
            }

            @Override
            public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
                return new Result.Ok<>(coder.createString(""));
            }
        };
    }
}
