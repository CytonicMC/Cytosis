package net.cytonic.cytosis.data.enums;

import net.cytonic.enums.ChatChannel;
import net.cytonic.objects.TypedNamespace;
import net.minestom.server.utils.NamespaceID;

import java.util.Set;

/**
 * A class holding all the Cytosis namespaces
 */
public class CytosisNamespaces {

    /**
     * A private constructor to prevent instantiation
     */
    private CytosisNamespaces() {
        // do nothing
    }

    /**
     * If the player accepts friend requests
     */
    public static final TypedNamespace<Boolean> ACCEPT_FRIEND_REQUESTS = new TypedNamespace<>(NamespaceID.from("cytosis", "accept_friend_request"), Boolean.class); // <Boolean>

    /**
     * If the player should recieve server alerts
     */
    public static final TypedNamespace<Boolean> SERVER_ALERTS = new TypedNamespace<>(NamespaceID.from("cytosis", "server_alerts"), Boolean.class); // <Boolean>

    /**
     * What chat channel the user is currently talking in
     */
    public static final TypedNamespace<ChatChannel> CHAT_CHANNEL = new TypedNamespace<>(NamespaceID.from("cytosis", "chat_channel"), ChatChannel.class); // <ChatChannel>

    /**
     * If the user should be vanished
     */
    public static final TypedNamespace<Boolean> VANISHED = new TypedNamespace<>(NamespaceID.from("cytosis", "vanished"), Boolean.class); // <Boolean>

    /**
     * A convienient set of all the namespaces
     */
    public static final Set<TypedNamespace<?>> ALL = Set.of(ACCEPT_FRIEND_REQUESTS, SERVER_ALERTS, CHAT_CHANNEL, VANISHED);
}
