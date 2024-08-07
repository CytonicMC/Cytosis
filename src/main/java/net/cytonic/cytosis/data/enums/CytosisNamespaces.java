package net.cytonic.cytosis.data.enums;

import net.cytonic.enums.ChatChannel;
import net.cytonic.objects.TypedNamespace;
import net.minestom.server.utils.NamespaceID;

import java.util.Set;

public class CytosisNamespaces {

    /**
     * A private constructor to prevent instantiation
     */
    private CytosisNamespaces() {
        // do nothing
    }

    public static final TypedNamespace<Boolean> ACCEPT_FRIEND_REQUESTS = new TypedNamespace<>(NamespaceID.from("cytosis", "accept_friend_request"), Boolean.class); // <Boolean>
    public static final TypedNamespace<Boolean> SERVER_ALERTS = new TypedNamespace<>(NamespaceID.from("cytosis", "server_alerts"), Boolean.class); // <Boolean>
    public static final TypedNamespace<ChatChannel> CHAT_CHANNEL = new TypedNamespace<>(NamespaceID.from("cytosis", "chat_channel"), ChatChannel.class); // <ChatChannel>
    public static final TypedNamespace<Boolean> VANISHED = new TypedNamespace<>(NamespaceID.from("cytosis", "vanished"), Boolean.class); // <Boolean>

    public static final Set<TypedNamespace<?>> ALL = Set.of(ACCEPT_FRIEND_REQUESTS, SERVER_ALERTS, CHAT_CHANNEL, VANISHED);
}
