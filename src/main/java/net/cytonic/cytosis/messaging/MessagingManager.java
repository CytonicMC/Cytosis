package net.cytonic.cytosis.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;
import net.cytonic.cytosis.config.CytosisSettings;

public class MessagingManager {

    @Getter
    private final RabbitMQ rabbitMQ;
    private final ExecutorService worker;

    public MessagingManager() {
        worker = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("CytosisMessageWorker").factory());
        if (CytosisSettings.RABBITMQ_ENABLED) this.rabbitMQ = new RabbitMQ();
        else this.rabbitMQ = null;
    }

    public CompletableFuture<Void> initialize() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        worker.submit(() -> {
            if (rabbitMQ != null) {
                rabbitMQ.initializeConnection();
                rabbitMQ.initializeQueues();
                rabbitMQ.sendServerDeclarationMessage();
                rabbitMQ.receiveChatMessages();
            }
            future.complete(null);
        });
        return future;
    }
}