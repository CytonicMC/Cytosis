package net.cytonic.cytosis.messaging;

import net.cytonic.cytosis.CytosisContext;

/**
 * A list of every subject used by the NATS server
 */
public class Subjects {
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
    public static final String FRIEND_REMOVED = "friends.removed";

    public static final String CHAT_MESSAGE = "chat.message";

    public static final String CREATE_SERVER = "servers.create";
    public static final String DELETE_ALL_SERVERS = "servers.delete.all";
    public static final String DELETE_SERVER = "servers.delete";
    public static final String UPDATE_SERVER = "servers.update";
}