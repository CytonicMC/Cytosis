package net.cytonic.cytosis.environments;

import lombok.Getter;

public enum Environment {
    ALPHA("alpha_"),
    DEVELOPMENT("dev_"),
    PRODUCTION("prod_");

    @Getter
    private final String prefix;

    Environment(String prefix) {
        this.prefix = prefix;
    }
}
