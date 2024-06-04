package net.cytonic.cytosis.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.OfflinePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.entity.Player;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ {

    public static final String SERVER_DECLARE_QUEUE = "server-declaration";
    public static final String SHUTDOWN_QUEUE = "server-shutdown";
    public static final String PLAYER_KICK_QUEUE = "player-kick";
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
        try {
            channel.queueDeclare(PLAYER_KICK_QUEUE, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occoured whilst initializing the 'PLAYER_KICK_QUEUE'.", e);
        }
    }

    public void sendServerDeclarationMessage() {
        //formatting: {server-name}|:|{server-ip}|:|{server-port}
        String serverIP;
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("An error occoured whilst fetching this server's IP address! Bailing out!", e);
            return;
        }
        String message = STR."\{Cytosis.SERVER_ID}|:|\{serverIP}|:|\{CytosisSettings.SERVER_PORT}";
        try {
            channel.basicPublish("", SERVER_DECLARE_QUEUE, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occoured whilst attempting to send the server declaration message!", e);
        }
        Logger.info(STR."Server Declaration message sent! '\{message}'.");
    }

    public void sendServerShutdownMessage() {
        //formatting: {server-name}|:|{server-ip}|:|{server-port}
        String serverIP;
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("An error occoured whilst fetching this server's IP address! Bailing out!", e);
            return;
        }
        String message = STR."\{Cytosis.SERVER_ID}|:|\{serverIP}|:|\{CytosisSettings.SERVER_PORT}";
        try {
            channel.basicPublish("", SHUTDOWN_QUEUE, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occoured whilst attempting to send the server declaration message!", e);
        }
        Logger.info(STR."Server Declaration message sent! '\{message}'.");
    }

    public void shutdown() {
        try {
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void kickPlayer(Player player, KickReason reason, Component message) {
        // FORMAT: {uuid}|:|{reason}|:|{name}|:|{message}|:|{rescuable}
        String rawMessage = STR."\{player.getUuid().toString()}|:|\{reason}|:|\{player.getUsername()}|:|\{JSONComponentSerializer.json().serialize(message)}|:|\{reason.isRescuable()}";
        try {
            channel.basicPublish("", PLAYER_KICK_QUEUE, null, rawMessage.getBytes());
        } catch (IOException e) {
            Logger.error(STR."An error occoured whilst attempting to kick the player \{player.getName()}.", e);
        }
    }

    public void kickPlayer(OfflinePlayer player, KickReason reason, Component message) {
        // FORMAT: {uuid}|:|{reason}|:|{name}|:|{message}|:|{rescuable}
        String rawMessage = STR."\{player.uuid().toString()}|:|\{reason}|:|\{player.name()}|:|\{JSONComponentSerializer.json().serialize(message)}|:|\{reason.isRescuable()}";
        try {
            channel.basicPublish("", PLAYER_KICK_QUEUE, null, rawMessage.getBytes());
        } catch (IOException e) {
            Logger.error(STR."An error occoured whilst attempting to kick the player \{player.name()}.", e);
        }
    }
}