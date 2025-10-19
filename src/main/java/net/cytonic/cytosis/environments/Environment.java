package net.cytonic.cytosis.environments;

import lombok.Getter;

public enum Environment {
    DEVELOPMENT("dev_"),
    ALPHA("alpha_"),
    PRODUCTION("prod_");

    @Getter
    private final String prefix;

    Environment(String prefix) {
        this.prefix = prefix;
    }
}
