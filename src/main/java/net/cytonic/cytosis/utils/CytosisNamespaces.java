package net.cytonic.cytosis.utils;

import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.data.containers.IgnoredChatChannelContainer;
import net.cytonic.cytosis.data.containers.snooper.SnoopsContainer;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.TypedNamespace;
import net.minestom.server.utils.NamespaceID;

import java.util.Set;

/**
 * A class holding all the Cytosis namespaces
 */
@UtilityClass
public class CytosisNamespaces {

    /**
     * If the player accepts friend requests
     */
    public static final TypedNamespace<Boolean> ACCEPT_FRIEND_REQUESTS = new TypedNamespace<>(NamespaceID.from("cytosis", "accept_friend_request"), Boolean.class); // <Boolean>

    /**
     * If the player should receive server alerts
     */
    public static final TypedNamespace<Boolean> SERVER_ALERTS = new TypedNamespace<>(NamespaceID.from("cytosis", "server_alerts"), Boolean.class); // <Boolean>

    /**
     * What chat channel the user is currently talking in
     */
    public static final TypedNamespace<ChatChannel> CHAT_CHANNEL = new TypedNamespace<>(NamespaceID.from("cytosis", "chat_channel"), ChatChannel.class); // <ChatChannel>

    /**
     * A preference to store the players ignored chat channels
     */
    public static final TypedNamespace<IgnoredChatChannelContainer> IGNORED_CHAT_CHANNELS = new TypedNamespace<>(NamespaceID.from("cytosis", "ignored_chat_channels"), IgnoredChatChannelContainer.class); // <String>

    /**
     * A preference to store the channels a player is snooping through
     */
    public static final TypedNamespace<SnoopsContainer> LISTENING_SNOOPS = new TypedNamespace<>(NamespaceID.from("cytosis", "listened_snoops"), SnoopsContainer.class); // <String>

    /**
     * A preference to store a players snooper mute status
     */
    public static final TypedNamespace<Boolean> MUTE_SNOOPER = new TypedNamespace<>(NamespaceID.from("cytosis", "mute_snoops"), Boolean.class);

    /**
     * If the user should be vanished
     */
    public static final TypedNamespace<Boolean> VANISHED = new TypedNamespace<>(NamespaceID.from("cytosis", "vanished"), Boolean.class); // <Boolean>

    /**
     * A convenient set of all the namespaces
     */
    public static final Set<TypedNamespace<?>> ALL = Set.of(ACCEPT_FRIEND_REQUESTS, SERVER_ALERTS, CHAT_CHANNEL, VANISHED, IGNORED_CHAT_CHANNELS, LISTENING_SNOOPS);
}
