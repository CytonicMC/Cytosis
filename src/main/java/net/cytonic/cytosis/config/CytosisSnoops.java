package net.cytonic.cytosis.config;

import net.kyori.adventure.key.Key;

import net.cytonic.cytosis.data.containers.snooper.SnooperChannel;

public class CytosisSnoops {

    private static final byte ALL_STAFF = (byte) (0x01 | 0x02 | 0x04 | 0x08); // owner, admin, mod, helper
    /**
     * Used whenever a moderator unmutes a player
     */
    public static final SnooperChannel PLAYER_UNMUTE = new SnooperChannel("cytosis.snooper.player.unmute",
        Key.key("cytosis:unmute"), ALL_STAFF);
    /**
     * Used whenever a moderator mutes a player
     */
    public static final SnooperChannel PLAYER_MUTE = new SnooperChannel("cytosis.snooper.player.mute",
        Key.key("cytosis:mute"), ALL_STAFF);
    /**
     * used whenever a staff member clears the chat
     */
    public static final SnooperChannel CHAT_CLEAR = new SnooperChannel("cytosis.snooper.chat.clear",
        Key.key("cytosis:clear"), ALL_STAFF);
    public static final SnooperChannel PLAYER_SERVER_CHANGE = new SnooperChannel("cytosis.snooper.player.server.change",
        Key.key("cytosis:switch_server"), ALL_STAFF);
    public static final SnooperChannel CHANGE_RANK = new SnooperChannel("cytosis.snooper.change_rank",
        Key.key("cytosis:change_rank"), ALL_STAFF);
    public static final SnooperChannel PLAYER_NICKNAME = new SnooperChannel("cytosis.snooper.player.nickname",
        Key.key("cytosis:nickname"), ALL_STAFF);
    private static final byte NOT_HELPER = (byte) (0x01 | 0x02 | 0x04); // owner, admin, mod
    private static final byte ADMIN = (byte) (0x01 | 0x02); // owner, admin
    public static final SnooperChannel SERVER_ERROR = new SnooperChannel("cytosis.snooper.server_error",
        Key.key("cytosis:server_error"), ADMIN);
    private static final byte MODERATOR = (byte) (0x01 | 0x04); // owner, moderator
    /**
     * Used whenever a moderator bans a player
     */
    public static final SnooperChannel PLAYER_BAN = new SnooperChannel("cytosis.snooper.player.bans",
        Key.key("cytosis:ban"), MODERATOR);
    /**
     * Used whenever a moderator unbans a player
     */
    public static final SnooperChannel PLAYER_UNBAN = new SnooperChannel("cytosis.snooper.player.unbans",
        Key.key("cytosis:unban"), MODERATOR);
    /**
     * Used whenever a moderator warns a player
     */
    public static final SnooperChannel PLAYER_WARN = new SnooperChannel("cytosis.snooper.player.warn",
        Key.key("cytosis:warn"), MODERATOR);

    /**
     * Used whenever a moderator kicks a player via commands. Not triggered on a server initiated kick
     */
    public static final SnooperChannel PLAYER_KICK = new SnooperChannel("cytosis.snooper.player.kick",
        Key.key("cytosis:kick"), MODERATOR);


}
