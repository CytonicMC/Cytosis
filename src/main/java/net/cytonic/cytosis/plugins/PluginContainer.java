package net.cytonic.cytosis.plugins;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginContainer {
    @Getter
    private final PluginDescription description;
    @Setter
    private CytosisPlugin instance;
    private volatile ExecutorService service;

    public PluginContainer(PluginDescription description) {
        this.description = description;
    }

    public Optional<CytosisPlugin> getInstance() {
        return Optional.ofNullable(instance);
    }

    public ExecutorService getExecutorService() {
        if (this.service == null) {
            synchronized (this) {
                if (this.service == null) {
                    String name = this.description.getName().orElse(this.description.getId());
                    this.service = Executors.unconfigurableExecutorService(
                            Executors.newCachedThreadPool(
                                    new ThreadFactoryBuilder().setDaemon(true)
                                            .setNameFormat(name + " - Task Executor #%d")
                                            .setDaemon(true)
                                            .build()
                            )
                    );
                }
            }
        }

        return this.service;
    }

    public boolean hasExecutorService() {
        return this.service != null;
    }
}
