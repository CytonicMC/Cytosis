package net.cytonic.cytosis.environments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minestom.server.codec.Codec;

@Getter
@AllArgsConstructor
public enum Environment {
    DEVELOPMENT("dev_"),
    ALPHA("alpha_"),
    PRODUCTION("prod_");
    public static final Codec<Environment> CODEC = Codec.Enum(Environment.class);
    private final String prefix;
}
