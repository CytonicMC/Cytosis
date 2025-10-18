package net.cytonic.cytosis.environments;

import lombok.Getter;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.logging.Logger;

public class EnvironmentManager implements Bootstrappable {

    @Getter
    private Environment environment = Environment.DEVELOPMENT;

    @Override
    public void init() {
        if (System.getenv("CYTONIC_ENVIRONMENT") != null) {
            environment = Environment.valueOf(System.getenv("CYTONIC_ENVIRONMENT"));
        }
        Logger.info("Starting in environment: %s", environment.name());
    }
}
