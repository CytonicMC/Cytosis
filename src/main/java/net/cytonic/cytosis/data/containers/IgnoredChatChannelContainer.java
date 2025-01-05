package net.cytonic.cytosis.data.containers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;


// pms get handled differently
public record IgnoredChatChannelContainer(boolean all, boolean party, boolean league, boolean admin, boolean mod,
                                          boolean staff) implements Cloneable {

    public static final IgnoredChatChannelContainer NONE = new IgnoredChatChannelContainer(false, false, false, false, false, false);

    public static IgnoredChatChannelContainer fromJson(String json) {
        return Cytosis.GSON.fromJson(json, IgnoredChatChannelContainer.class);
    }

    public String toJson() {
        return Cytosis.GSON.toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }

    public IgnoredChatChannelContainer withAdmin(boolean admin) {
        return new IgnoredChatChannelContainer(all, party, league, admin, mod, staff);
    }

    public IgnoredChatChannelContainer withStaff(boolean staff) {
        return new IgnoredChatChannelContainer(all, party, league, admin, mod, staff);
    }

    public IgnoredChatChannelContainer withMod(boolean mod) {
        return new IgnoredChatChannelContainer(all, party, league, admin, mod, staff);
    }

    public IgnoredChatChannelContainer withAll(boolean all) {
        return new IgnoredChatChannelContainer(all, party, league, admin, mod, staff);
    }

    public IgnoredChatChannelContainer withLeague(boolean league) {
        return new IgnoredChatChannelContainer(all, party, league, admin, mod, staff);
    }

    public IgnoredChatChannelContainer withParty(boolean party) {
        return new IgnoredChatChannelContainer(all, party, league, admin, mod, staff);
    }

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


    @Override
    public Object clone() {
        // primitives are passed by value not reference
        return new IgnoredChatChannelContainer(all, party, league, admin, mod, staff);
    }
}
