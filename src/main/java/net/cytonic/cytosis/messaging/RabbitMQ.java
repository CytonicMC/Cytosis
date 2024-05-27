package net.cytonic.cytosis.messaging;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
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
    public static final String CHAT_CHANNEL_QUEUE = STR."chat-channel-\{Cytosis.SERVER_ID}";
    public static final String CHAT_CHANNEL_QUEUE = "chat-channel";
    public static final String PLAYER_KICK_QUEUE = "player-kick";
    public static final String CHAT_CHANNEL_EXCHANGE = "chat-exchange";
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
            Logger.error("An error occurred whilst connecting to RabbitMQ!", e);
        }
        Logger.info("Connected to RabbitMQ!");
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            Logger.error("An error occurred whilst connecting to RabbitMQ!", e);
        }
    }

    public void initializeQueues() {
        Logger.info("Initializing RabbitMQ queues...");
        try {
            channel.queueDeclare(SERVER_DECLARE_QUEUE, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occurred whilst initializing the 'SERVER_DECLARE_QUEUE'.", e);
        }
        try {
            channel.queueDeclare(SHUTDOWN_QUEUE, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occurred whilst initializing the 'SHUTDOWN_QUEUE'.", e);
        }
        try {
            channel.exchangeDeclare(CHAT_CHANNEL_EXCHANGE, BuiltinExchangeType.FANOUT);
            channel.queueDeclare(CHAT_CHANNEL_QUEUE, false, false, false, null);
            channel.queueBind(CHAT_CHANNEL_QUEUE, CHAT_CHANNEL_EXCHANGE, "");
        } catch (IOException e) {
            Logger.error("An error occurred whilst initializing the 'CHAT_CHANNEL_QUEUE'.", e);
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
            Logger.error("An error occurred whilst fetching this server's IP address! Bailing out!", e);
            return;
        }
        String message = STR."\{Cytosis.SERVER_ID}|:|\{serverIP}|:|\{CytosisSettings.SERVER_PORT}";
        try {
            channel.basicPublish("", SERVER_DECLARE_QUEUE, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to send the server declaration message!", e);
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

    public void sendChatMessage(Component chatMessage, ChatChannel chatChannel) {
        //formatting: {chat-message}|{chat-channel}
        String message = STR."\{JSONComponentSerializer.json().serialize(chatMessage)}|\{chatChannel.name()}";
        try {
            channel.basicPublish("chat-messages", CHAT_CHANNEL_QUEUE, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to send a chat message!", e);
        }
    }
    /**
     * Sends a chat message to all the servers.
     *
     * @param chatMessage The chat message to be sent. This should be a {@link Component}.
     * @param chatChannel The channel to which the chat message should be sent.
     */
    public void sendChatMessage(Component chatMessage, ChatChannel chatChannel) {
        // Formatting: {chat-message}|:|{chat-channel}
        String message = STR."\{JSONComponentSerializer.json().serialize(chatMessage)}|\{chatChannel.name()}";
        try {
            channel.basicPublish(CHAT_CHANNEL_EXCHANGE, "", null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to send a chat message!", e);
        }
    }

    public void receiveChatMessages() {
        try {
            DeliverCallback deliverCallback = (_, delivery) -> {
                String[] thing = new String(delivery.getBody(), StandardCharsets.UTF_8).split("\\|");
                Component chatMessage = JSONComponentSerializer.json().deserialize(thing[0]);
                ChatChannel chatChannel = ChatChannel.valueOf(thing[1]);
                switch (chatChannel) {
                    case MOD -> // send a message to all players with cytonic.chat.mod permission
                            Cytosis.getOnlinePlayers().forEach(player -> {
                                if (player.hasPermission("cytonic.chat.mod")) {
                                    player.sendMessage(chatMessage);
                                }
                            });

                    case STAFF -> // send a message to all players with cytonic.chat.staff permission
                            Cytosis.getOnlinePlayers().forEach(player -> {
                                if (player.hasPermission("cytonic.chat.staff")) {
                                    player.sendMessage(chatMessage);
                                }
                            });
                    case ADMIN -> // send a message to all players with cytonic.chat.admin permission
                            Cytosis.getOnlinePlayers().forEach(player -> {
                                if (player.hasPermission("cytonic.chat.admin")) {
                                    player.sendMessage(chatMessage);
                                }
                            });
                    case LEAGUE -> {// leagues..
                    }
            case STAFF -> // send a message to all players with cytonic.chat.staff permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.staff")) {
                            player.sendMessage(chatMessage);
                        }
            });
            case ADMIN -> // send a message to all players with cytonic.chat.admin permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.admin")) {
                            player.sendMessage(chatMessage);
                        }
            });
            case LEAGUE -> {
            // leagues..
            }

                    case PARTY -> {// parties..
                    }
                }
            };
            channel.basicConsume(CHAT_CHANNEL_QUEUE, true, deliverCallback, _ -> {});
        } catch (IOException e) {
            Logger.error("error", e);
        }
    }
}