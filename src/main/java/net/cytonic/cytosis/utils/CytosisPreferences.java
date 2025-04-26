package net.cytonic.cytosis.utils;

import lombok.experimental.UtilityClass;
import net.cytonic.cytosis.data.containers.IgnoredChatChannelContainer;
import net.cytonic.cytosis.data.containers.snooper.SnoopsContainer;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.preferences.JsonPreference;
import net.cytonic.cytosis.data.objects.preferences.NamespacedPreference;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.nicknames.NicknameManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A list of Cytosis supplied preferences
 */
@UtilityClass
public class CytosisPreferences {
    /**
     * A preference to accept or decline friend requests, type of BOOLEAN
     */
    public static final NamespacedPreference<Boolean> ACCEPT_FRIEND_REQUESTS = new NamespacedPreference<>(CytosisNamespaces.ACCEPT_FRIEND_REQUESTS, true);
    /**
     * A preference to send or not send server alerts, type of BOOLEAN
     */
    public static final NamespacedPreference<Boolean> SERVER_ALERTS = new NamespacedPreference<>(CytosisNamespaces.SERVER_ALERTS, false);
    /**
     * A preference to store the players chat channel, type of ChatChannel
     */
    public static final NamespacedPreference<ChatChannel> CHAT_CHANNEL = new NamespacedPreference<>(CytosisNamespaces.CHAT_CHANNEL, ChatChannel.ALL);
    /**
     * A preference to store the players ignored chat channels, type of IgnoredChatChannelContainer
     */
    public static final JsonPreference<IgnoredChatChannelContainer> IGNORED_CHAT_CHANNELS = new JsonPreference<>(CytosisNamespaces.IGNORED_CHAT_CHANNELS, IgnoredChatChannelContainer.NONE);
    /**
     * A preference to store the channels the player is snooping through
     */
    public static final JsonPreference<SnoopsContainer> LISTENING_SNOOPS = new JsonPreference<>(CytosisNamespaces.LISTENING_SNOOPS, new SnoopsContainer(new HashSet<>()));

    public static final Preference<Boolean> MUTE_SNOOPER = new NamespacedPreference<>(CytosisNamespaces.MUTE_SNOOPER, false);

    /**
     * A preference if the player is vanished, type of BOOLEAN
     */
    public static final NamespacedPreference<Boolean> VANISHED = new NamespacedPreference<>(CytosisNamespaces.VANISHED, false);

    public static final JsonPreference<NicknameManager.NicknameData> NICKNAME_DATA = new JsonPreference<>(CytosisNamespaces.NICKNAME_DATA, null);

    public static final NamespacedPreference<UUID> NICKED_UUID = new NamespacedPreference<>(CytosisNamespaces.NICKED_UUID, null);

    /**
     * A set of all the preferences that are available here.
     */
    public static final Set<NamespacedPreference<?>> ALL = Set.of(ACCEPT_FRIEND_REQUESTS, SERVER_ALERTS, CHAT_CHANNEL, VANISHED, IGNORED_CHAT_CHANNELS, LISTENING_SNOOPS);
}
