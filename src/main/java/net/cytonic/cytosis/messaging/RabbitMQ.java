package net.cytonic.cytosis.messaging;

import com.rabbitmq.client.*;
import lombok.NoArgsConstructor;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A class holding a connection to the RabbitMQ server.
 * <p>
 * It serves as a wrapper around the RabbitMQ Java API,
 * facilitating the communication between other instances of Cytosis and Cynturion.
 */
@SuppressWarnings("unused")
@NoArgsConstructor
public class RabbitMQ {

    private Connection connection;
    private Channel channel;

    /**
     * Initializes a connection to the RabbitMQ server.
     */
    public void initializeConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(CytosisSettings.RABBITMQ_HOST);
        factory.setPassword(CytosisSettings.RABBITMQ_PASSWORD);
        factory.setUsername(CytosisSettings.RABBITMQ_USERNAME);
        factory.setPort(CytosisSettings.RABBITMQ_PORT);
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            Logger.error("An error occurred whilst connecting to RabbitMQ!", e);
        }

        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            Logger.error("An error occurred whilst connecting to RabbitMQ!", e);
        }
        Logger.info("Connected to RabbitMQ!");
    }

    /**
     * Creates the queues required for Cytosis to link with proxy servers
     */
    public void initializeQueues() {
        Logger.info("Initializing RabbitMQ queues...");
    }

    /**
     * Closes the RabbitMQ connection
     */
    public void shutdown() {
        try {
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * RabbitMQ via the Plugins API
     */

    /**
     * Registers a queue with RabbitMQ
     *
     * @param queue the queue to register
     */
    public void registerQueue(String queue) {
        try {
            channel.queueDeclare(queue, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to register the queue '" + queue + "'", e);
        }
    }

    /**
     * Registers an exchange with RabbitMQ
     *
     * @param exchange The name of the exchange
     * @param type     The type of the exchange
     */
    public void registerExchange(String exchange, String type) {
        try {
            channel.exchangeDeclare(exchange, type);
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to register the exchange '" + exchange + "'", e);
        }
    }

    /**
     * Binds a queue to an exchange
     *
     * @param queue      The queue to bind
     * @param exchange   The exchange to bind the queue to
     * @param routingKey The routing key
     */
    public void bindQueue(String queue, String exchange, String routingKey) {
        try {
            channel.queueBind(queue, exchange, routingKey);
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to bind the queue '" + queue + "'", e);
        }
    }

    /**
     * Creates a callback for listening to messages on the specified queue
     *
     * @param queue           The queue to listen on
     * @param deliverCallback The callback for message delivery
     */
    public void consumeQueue(String queue, DeliverCallback deliverCallback) {
        try {
            channel.basicConsume(queue, true, deliverCallback, string -> {
            });
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to consume the queue! '" + queue + "'", e);
        }
    }

    /**
     * Sets a callback to consume messages on a queue
     *
     * @param queue           The queue to consume
     * @param deliverCallback The callback for message delivery
     * @param cancelCallback  The callback for consumer cancellation
     */
    public void consumeQueue(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback) {
        try {
            channel.basicConsume(queue, true, deliverCallback, cancelCallback);
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to consume the queue! '" + queue + "'", e);
        }
    }

    /**
     * Sends a message to a queue
     *
     * @param queue   The queue to send to
     * @param message The message to send
     */
    public void sendMessage(String queue, String message) {
        try {
            channel.basicPublish("", queue, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to send a message to the queue! '" + queue + "'", e);
        }
    }

    /**
     * Sends a message to an exchange and queue
     *
     * @param queue    The queue to send the message to
     * @param exchange The exchange to send the message on
     * @param message  The message to send
     */
    public void sendMessage(String queue, String exchange, String message) {
        try {
            channel.basicPublish(exchange, queue, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to send a message to the queue! '" + queue + "' on exchange '" + exchange + "'", e);
        }
    }

    /**
     * Rejects a message and optionally requeues it for delivery
     *
     * @param deliveryTag The delivery tag of the message envelope
     * @param requeue     if the message should be requeued for another delivery attempt
     */
    public void reject(long deliveryTag, boolean requeue) {
        try {
            channel.basicReject(deliveryTag, requeue);
        } catch (IOException e) {
            Logger.error("An error occurred whilst rejecting a message!", e);
        }
    }
}