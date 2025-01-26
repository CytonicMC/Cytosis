package net.cytonic.cytosis.config;

import net.cytonic.cytosis.data.containers.snooper.SnooperChannel;
import net.minestom.server.utils.NamespaceID;

public class CytosisSnoops {
    private static final byte ALL_STAFF = (byte) (0x01 | 0x02 | 0x04 | 0x08); // owner, admin, mod, helper
    /**
     * Used whenever a moderator bans a player
     */
    public static final SnooperChannel PLAYER_BANS = new SnooperChannel("cytosis.snooper.player.bans", NamespaceID.from("cytosis:snooper_ban"), ALL_STAFF);
    private static final byte NOT_HELPER = (byte) (0x01 | 0x02 | 0x04); // owner, admin, mod
    private static final byte ADMIN = (byte) (0x01 | 0x02); // owner, admin
    private static final byte MODERATOR = (byte) (0x01 | 0x04); // owner, moderator
}
