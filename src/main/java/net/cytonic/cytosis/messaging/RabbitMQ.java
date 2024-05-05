package net.cytonic.cytosis.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ {
    public static final String SERVER_DECLARE_QUEUE = "server-declaration";
    public static final String SHUTDOWN_QUEUE = "server-shutdown";
    private Connection connection;
    private Channel channel;

    public void initializeConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(CytosisSettings.RABBITMQ_HOST);
        factory.setPassword(CytosisSettings.RABBITMQ_PASSWORD);
        factory.setUsername(CytosisSettings.RABBITMQ_USERNAME);
        factory.setPort(CytosisSettings.RABBITMQ_PORT);

        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            Logger.error("An error occoured whilst connecting to RabbitMQ!", e);
        }

        Logger.info("Connected to RabbitMQ!");

        try {
            channel = connection.createChannel();
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            Logger.error("An error occoured whilst connecting to RabbitMQ!", e);
        }
    }

    public void initializeQueues() {
        Logger.info("Initializing RabbitMQ queues...");
        try {
            channel.queueDeclare(SERVER_DECLARE_QUEUE, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occoured whilst initializing the 'SERVER_DECLARE_QUEUE'.", e);
        }
        try {
            channel.queueDeclare(SHUTDOWN_QUEUE, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occoured whilst initializing the 'SHUTDOWN_QUEUE'.", e);
        }
    }

    public void sendServerDeclarationMessage() {
        //formatting: {server-name}|:|{server-ip}|:|{server-port}
        String serverName = System.getenv("HOSTNAME");
        String serverIP;

        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("An error occoured whilst fetching this server's IP address! Bailing out!", e);
            return;
        }

        int port = 25565;

        String message = STR."\{serverName}|:|\{serverIP}|:|\{port}";
        try {
            channel.basicPublish("", SERVER_DECLARE_QUEUE, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occoured whilst attempting to send the server declaration message!", e);
        }
        Logger.info(STR."Server Declaration message sent! '\{message}'.");
    }
}
