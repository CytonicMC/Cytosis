package net.cytonic.cytosis.data.enums;

import net.cytonic.enums.ChatChannel;
import net.cytonic.objects.NamespacedPreference;
import net.minestom.server.utils.NamespaceID;

/**
 * A list of Cytosis supplied preferences
 */
public class CytosisPreferences {
    /**
     * A private constructor to prevent instantiation
     */
    private CytosisPreferences() {
        // do nothing
    }

    /**
     * A preference to accept or decline friend requests, type of BOOLEAN
     */
    public static final NamespacedPreference<Boolean> ACCEPT_FRIEND_REQUESTS = new NamespacedPreference<>(NamespaceID.from("cytosis", "accept_friend_request"), true);

    /**
     * A preference to send or not send server alerts, type of BOOLEAN
     */
    public static final NamespacedPreference<Boolean> SERVER_ALERTS = new NamespacedPreference<>(NamespaceID.from("cytosis", "server_alerts"), false);

    /**
     * A preference to store the players chat channel, type of ChatChannel
     */
    public static final NamespacedPreference<ChatChannel> CHAT_CHANNEL = new NamespacedPreference<>(NamespaceID.from("cytosis", "chat_channel"), ChatChannel.ALL);
}
