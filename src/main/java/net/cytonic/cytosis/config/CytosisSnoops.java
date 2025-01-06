package net.cytonic.cytosis.config;

import net.cytonic.cytosis.data.containers.snooper.SnooperChannel;
import net.minestom.server.utils.NamespaceID;

public class CytosisSnoops {
    public static final byte ALL_STAFF = (byte) (0x01 | 0x02 | 0x04 | 0x08); // owner, admin, mod, helper
    public static final byte NOT_HELPER = (byte) (0x01 | 0x02 | 0x04); // owner, admin, mod
    public static final byte ADMIN = (byte) (0x01 | 0x02); // owner, admin
    public static final byte MODERATOR = (byte) (0x01 | 0x04); // owner, moderator

    /**
     * Used whenever a moderator bans a player
     */
    public static final SnooperChannel PLAYER_BANS = new SnooperChannel("cytosis.snooper.player.bans", NamespaceID.from("cytosis:snooper"), ALL_STAFF);
}
