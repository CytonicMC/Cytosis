package net.cytonic.cytosis.config;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.cytonic.cytosis.snooper.SnooperChannel;

@UtilityClass
@Internal
public class Snoops {

    private static final byte ALL_STAFF = (byte) (0x01 | 0x02 | 0x04 | 0x08); // owner, admin, mod, helper
    private static final byte NOT_HELPER = (byte) (0x01 | 0x02 | 0x04); // owner, admin, mod
    private static final byte ADMIN = (byte) (0x01 | 0x02); // owner, admin
    private static final byte MODERATOR = (byte) (0x01 | 0x04); // owner, moderator

    private static final List<SnooperChannel> REGISTRY = new ArrayList<>();

    /**
     * Used whenever a moderator unmutes a player
     */
    public static final SnooperChannel PLAYER_UNMUTE = make("player.unmute", "cytosis:unmute", ALL_STAFF);
    /**
     * Used whenever a moderator mutes a player
     */
    public static final SnooperChannel PLAYER_MUTE = make("player.mute", "cytosis:mute", ALL_STAFF);
    /**
     * used whenever a staff member clears the chat
     */
    public static final SnooperChannel CHAT_CLEAR = make("chat.clear", "cytosis:clear", ALL_STAFF);
    public static final SnooperChannel PLAYER_SERVER_CHANGE = make("player.server.change", "cytosis:switch_server",
        ALL_STAFF);
    public static final SnooperChannel CHANGE_RANK = make("change_rank", "cytosis:change_rank", ALL_STAFF);
    public static final SnooperChannel PLAYER_NICKNAME = make("player.nickname", "cytosis:nickname", ALL_STAFF);
    /**
     * Used whenever a staff member modifies the network whitelist
     */
    public static final SnooperChannel PLAYER_WHITELIST = make("player.whitelist", "cytosis:whitelist", ALL_STAFF);
    public static final SnooperChannel SERVER_ERROR = make("server_error", "cytosis:server_error", ADMIN);
    /**
     * Used whenever a moderator bans a player
     */
    public static final SnooperChannel PLAYER_BAN = make("player.bans", "cytosis:ban", MODERATOR);
    /**
     * Used whenever a moderator unbans a player
     */
    public static final SnooperChannel PLAYER_UNBAN = make("player.unbans", "cytosis:unban", MODERATOR);
    /**
     * Used whenever a moderator kicks a player via commands. Not triggered on a server initiated kick
     */
    public static final SnooperChannel PLAYER_KICK = make("player.kick", "cytosis:kick", MODERATOR);
    /**
     * Used whenever a report is submitted.
     */
    public static final SnooperChannel REPORT_SUBMITTED = make("report.submitted", "cytosis:report_submitted",
        MODERATOR);

    private static SnooperChannel make(String channel, String key, byte permission) {
        SnooperChannel c = new SnooperChannel("cytosis." + channel, Key.key(key), permission);
        REGISTRY.add(c);
        return c;
    }

    public static List<SnooperChannel> getRegistry() {
        return REGISTRY;
    }

}
