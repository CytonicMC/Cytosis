package net.cytonic.cytosis.messaging;

import lombok.Getter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.RedisDatabase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class to handle messaging
 */
public class MessagingManager {

    @Getter
    private final RabbitMQ rabbitMQ;
    private final RedisDatabase redis;
    private final ExecutorService worker;

    /**
     * Creates a new MessagingManager with a RabbitMQ instance and a worker thread
     */
    public MessagingManager() {
        worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisMessageWorker").factory());
        if (CytosisSettings.RABBITMQ_ENABLED) this.rabbitMQ = new RabbitMQ();
        else this.rabbitMQ = null;
        this.redis = Cytosis.getDatabaseManager().getRedisDatabase();
    }

    /**
     * Initializes the messaging systems
     *
     * @return a future that completes when the initialization is complete
     */
    public CompletableFuture<Void> initialize() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            if (rabbitMQ != null) {
                rabbitMQ.initializeConnection();
                rabbitMQ.initializeQueues();
                rabbitMQ.receiveChatMessages();
            }
            future.complete(null);
        });
        return future;
    }

    /**
     * Shuts down the manager by disconnecting from the RabbitMQ instance and shutting down the worker thread
     */
    public void shutdown() {
        if (rabbitMQ != null) {
            rabbitMQ.shutdown();
        }
        redis.sendShutdownMessage();
        worker.shutdown();
    }
}