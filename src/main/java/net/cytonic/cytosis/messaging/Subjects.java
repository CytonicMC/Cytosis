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

    public static final String SERVER_REGISTER = PREFIX + "servers.register";
    public static final String SERVER_SHUTDOWN = PREFIX + "servers.shutdown";
    public static final String SERVER_LIST = PREFIX + "servers.list";
    public static final String SERVER_SHUTDOWN_NOTIFY = PREFIX + "servers.proxy.shutdown.notify";
    public static final String PROXY_START = PREFIX + "servers.proxy.startup";

    public static final String PLAYER_LEAVE = PREFIX + "players.disconnect";
    public static final String PLAYER_JOIN = PREFIX + "players.connect";
    public static final String PLAYER_KICK = PREFIX + "players.kick";
    public static final String PLAYER_SEND = PREFIX + "players.send";
    public static final String PLAYER_SEND_GENERIC = PREFIX + "players.send.generic";
    public static final String PLAYER_SERVER_CHANGE = PREFIX + "players.server_change.notify";
    public static final String PLAYER_RANK_UPDATE = PREFIX + "players.rank.update";

    public static final String HEALTH_CHECK = PREFIX + "health.check." + CytosisContext.SERVER_ID;

    // friends
    public static final String FRIEND_REQUEST = PREFIX + "friends.request";
    public static final String FRIEND_ACCEPT = PREFIX + "friends.accept";
    public static final String FRIEND_DECLINE = PREFIX + "friends.decline";
    public static final String FRIEND_ACCEPT_BY_ID = PREFIX + "friends.accept.by_id";
    public static final String FRIEND_DECLINE_BY_ID = PREFIX + "friends.decline.by_id";
    public static final String FRIEND_ACCEPTANCE_NOTIFY = PREFIX + "friends.accept.notify";
    public static final String FRIEND_DECLINATION_NOTIFY = PREFIX + "friends.decline.notify";
    public static final String FRIEND_EXPIRE_NOTIFY = PREFIX + "friends.expire.notify";
    public static final String FRIEND_REQUEST_NOTIFY = PREFIX + "friends.request.notify";
    public static final String FRIEND_REMOVED = PREFIX + "friends.removed";

    public static final String CHAT_MESSAGE = PREFIX + "chat.message";
    public static final String CHAT_BROADCAST = PREFIX + "chat.broadcast";

    public static final String COOLDOWN_UPDATE = PREFIX + "cooldown.update";

    public static final String CREATE_SERVER = PREFIX + "servers.create";
    public static final String DELETE_ALL_SERVERS = PREFIX + "servers.delete.all";
    public static final String DELETE_SERVER = PREFIX + "servers.delete";
    public static final String UPDATE_SERVER = PREFIX + "servers.update";
}