package net.cytonic.cytosis.messaging;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.CytosisContext;
import net.cytonic.cytosis.environments.EnvironmentManager;

/**
 * A list of every subject used by the NATS server
 */
public class Subjects {

    public static final String PREFIX = Cytosis.CONTEXT
        .getComponent(EnvironmentManager.class)
        .getEnvironment()
        .getPrefix();

    //parties
    public static final String PARTY_JOIN_REQUEST_BYPASS = "party.join.request.bypass";

    public static final String SERVER_REGISTER = "servers.register";
    public static final String SERVER_SHUTDOWN = "servers.shutdown";
    public static final String SERVER_LIST = "servers.list";
    public static final String SERVER_SHUTDOWN_NOTIFY = "servers.proxy.shutdown.notify";
    public static final String PROXY_START = "servers.proxy.startup";

    public static final String PLAYER_LEAVE = "players.disconnect";
    public static final String PLAYER_JOIN = "players.connect";
    public static final String PLAYER_KICK = "players.kick";
    public static final String PLAYER_SEND = "players.send";
    public static final String PLAYER_SEND_GENERIC = "players.send.generic";
    public static final String PLAYER_SERVER_CHANGE = "players.server_change.notify";
    public static final String PLAYER_RANK_UPDATE = "players.rank.update";

    public static final String HEALTH_CHECK = "health.check." + CytosisContext.SERVER_ID;

    // friends
    public static final String FRIEND_REQUEST = "friends.request";
    public static final String FRIEND_ACCEPT = "friends.accept";
    public static final String FRIEND_DECLINE = "friends.decline";
    public static final String FRIEND_ACCEPT_BY_ID = "friends.accept.by_id";
    public static final String FRIEND_DECLINE_BY_ID = "friends.decline.by_id";
    public static final String FRIEND_ACCEPTANCE_NOTIFY = "friends.accept.notify";
    public static final String FRIEND_DECLINATION_NOTIFY = "friends.decline.notify";
    public static final String FRIEND_EXPIRE_NOTIFY = "friends.expire.notify";
    public static final String FRIEND_REQUEST_NOTIFY = "friends.request.notify";
    public static final String FRIEND_REMOVE = "friends.removed";

    public static final String CHAT_MESSAGE = "chat.message";
    public static final String CHAT_BROADCAST = "chat.broadcast";

    public static final String COOLDOWN_UPDATE_NOTIFY = "cooldown.update.*"; // personal vs global
    public static final String COOLDOWN_UPDATE_PERSONAL = "cooldown.update.personal";
    public static final String COOLDOWN_UPDATE_GLOBAL = "cooldown.update.global";

    public static final String CREATE_SERVER = "servers.create";
    public static final String DELETE_ALL_SERVERS = "servers.delete.all";
    public static final String DELETE_SERVER = "servers.delete";
    public static final String UPDATE_SERVER = "servers.update";
    public static final String PARTY_JOIN_REQUEST_COMMAND = "party.join.request.command";
    public static final String PARTY_TRANSFER_NOTIFY = "party.transfer.notify.*";
    public static final String PARTY_JOIN_NOTIFY = "party.join.notify";

    public static final String PARTY_LEAVE_REQUEST = "party.leave.request";
    public static final String PARTY_LEAVE_NOTIFY = "party.leave.notify.*";

    public static final String PARTY_CREATE_NOTIFY = "party.create.notify";

    public static final String PARTY_PROMOTE_REQUEST = "party.promote.request";
    public static final String PARTY_PROMOTE_NOTIFY = "party.promote.notify.*";

    public static final String PARTY_KICK_REQUEST = "party.kick.request";
    public static final String PARTY_KICK_NOTIFY = "party.kick.notify";

    public static final String PARTY_TRANSFER_REQUEST = "party.transfer.request";

    public static String applyPrefix(String subject) {
        if (subject.startsWith("_INBOX")) { // prevent replies from being inadvertently prefixed
            return subject;
        }
        if (subject.startsWith(PREFIX)) {
            return subject;
        }
        return PREFIX + subject;
    }

    public static final String PARTY_STATE_MUTE_REQUEST = "party.state.mute.request";
    public static final String PARTY_STATE_OPEN_REQUEST = "party.state.open.request";
    public static final String PARTY_STATE_OPEN_INVITES_REQUEST = "party.state.open_invites.request";
    public static final String PARTY_STATE_NOTIFY = "party.state.*.notify";

    public static final String PARTY_YOINK_REQUEST = "party.yoink.request";
    public static final String PARTY_YOINK_NOTIFY = "party.yoink.notify";

    public static final String PARTY_DISBAND_REQUEST = "party.disband.request";
    public static final String PARTY_DISBAND_NOTIFY = "party.disband.notify.*";

    public static final String PARTY_STATUS_NOTIFY = "party.status.*.notify";
    public static final String PARTY_INVITE_ACCEPT_REQUEST = "party.invites.accept";
    public static final String PARTY_INVITE_SEND_REQUEST = "party.invites.send";
    public static final String PARTY_INVITE_SEND_NOTIFY = "party.invites.send.notify";
    public static final String PARTY_INVITE_EXPIRE_NOTIFY = "party.invites.expire";

}