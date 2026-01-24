package net.cytonic.cytosis.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.cytosis.data.containers.IgnoredChatChannelContainer;
import net.cytonic.cytosis.data.containers.SnoopsContainer;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.preferences.JsonPreference;
import net.cytonic.cytosis.data.objects.preferences.Preference;
import net.cytonic.cytosis.nicknames.NicknameManager.NicknameData;

/**
 * A list of Cytosis supplied preferences
 */
@UtilityClass
@Internal
public class Preferences {

    /**
     * A set of all the preferences that are available here.
     */
    public static final Set<Preference<?>> ALL = new HashSet<>();

    public static final Preference<Boolean> ACCEPT_FRIEND_REQUESTS = make("accept_friend_request", true);
    public static final Preference<Boolean> SERVER_ALERTS = make("server_alerts", false);
    public static final Preference<ChatChannel> CHAT_CHANNEL = make("chat_channel", ChatChannel.ALL);
    public static final JsonPreference<IgnoredChatChannelContainer> IGNORED_CHAT_CHANNELS = makeJson(
        "ignored_chat_channels", IgnoredChatChannelContainer.NONE);
    public static final JsonPreference<SnoopsContainer> LISTENING_SNOOPS = makeJson("listened_snoops",
        new SnoopsContainer(new HashSet<>()));
    public static final Preference<Boolean> MUTE_SNOOPER = make("mute_snoops", false);
    public static final Preference<Boolean> VANISHED = make("vanished", false);
    public static final JsonPreference<NicknameData> NICKNAME_DATA = makeJson("nickname_data", NicknameData.class);
    public static final Preference<UUID> NICKED_UUID = make("nicked_uuid", UUID.class);
    public static final Preference<Boolean> CHAT_MESSAGE_PING = make("chat_message_ping", false);
    public static final Preference<Boolean> TPS_DEBUG = make("tps_debug", false);
    public static final Preference<Boolean> FLY = make("fly", false);
    public static final Preference<Float> FLY_SPEED = make("fly_speed", 1F);


    private static <T> Preference<T> make(String key, T def) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) def.getClass();

        Preference<T> val = new Preference<>(type, Key.key("cytosis", key), def);
        ALL.add(val);
        return val;
    }

    private static <T> Preference<T> make(String key, Class<T> clazz) {
        Preference<T> val = new Preference<>(clazz, Key.key("cytosis", key), null);
        ALL.add(val);
        return val;
    }

    private static <T> JsonPreference<T> makeJson(String key, T def) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) def.getClass();

        JsonPreference<T> val = new JsonPreference<>(Key.key("cytosis", key), type, def);
        ALL.add(val);
        return val;
    }

    private static <T> JsonPreference<T> makeJson(String key, Class<T> clazz) {
        JsonPreference<T> val = new JsonPreference<>(Key.key("cytosis", key), clazz, null);
        ALL.add(val);
        return val;
    }
}
