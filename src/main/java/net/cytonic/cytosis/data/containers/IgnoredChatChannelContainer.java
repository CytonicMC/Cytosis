package net.cytonic.cytosis.data.containers;

import lombok.With;
import net.cytonic.cytosis.data.enums.ChatChannel;

@With
public record IgnoredChatChannelContainer(boolean all, boolean party, boolean league, boolean admin, boolean mod,
                                          boolean staff) {

    public static final IgnoredChatChannelContainer NONE =
            new IgnoredChatChannelContainer(false, false, false, false, false, false);

    public boolean getForChannel(ChatChannel channel) {
        return switch (channel) {
            case ADMIN -> admin();
            case ALL -> all();
            case LEAGUE -> league();
            case MOD -> mod();
            case PARTY -> party();
            case STAFF -> staff();
            default -> false;
        };
    }

    public IgnoredChatChannelContainer withForChannel(ChatChannel channel, boolean val) {
        return switch (channel) {
            case ADMIN -> withAdmin(val);
            case ALL -> withAll(val);
            case LEAGUE -> withLeague(val);
            case MOD -> withMod(val);
            case PARTY -> withParty(val);
            case STAFF -> withStaff(val);
            default -> NONE;
        };
    }
}
